package com.restaurant.manager.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restaurant.manager.client.AiServiceClient;
import com.restaurant.manager.dto.response.ChatSessionResponse;
import com.restaurant.manager.entity.Area;
import com.restaurant.manager.entity.Category;
import com.restaurant.manager.entity.ChatMessage;
import com.restaurant.manager.entity.ChatSession;
import com.restaurant.manager.entity.Tag;
import com.restaurant.manager.exception.ApiException;
import com.restaurant.manager.repository.AreaRepository;
import com.restaurant.manager.repository.CategoryRepository;
import com.restaurant.manager.repository.ChatMessageRepository;
import com.restaurant.manager.repository.ChatSessionRepository;
import com.restaurant.manager.repository.DiningTableRepository;
import com.restaurant.manager.repository.ItemRepository;
import com.restaurant.manager.repository.TagRepository;
import com.restaurant.manager.repository.UserRepository;
import com.restaurant.manager.security.CurrentUser;

/**
 * Trợ lý hội thoại: ghép ngữ cảnh (thực đơn có trạng thái còn/hết, dị ứng của khách, bàn trống, lịch sử) rồi gọi AI service.
 * AI service lỗi thì vẫn trả lời bằng món phổ biến đã lọc dị ứng và món hết, để trợ lý không ngừng hoạt động.
 */
@Service
public class ChatService {

	private static final Logger log = LoggerFactory.getLogger(ChatService.class);
	private static final int RATE_PER_MINUTE = 20; // bảo vệ hạn mức gọi LLM

	private final ChatSessionRepository sessions;
	private final ChatMessageRepository messages;
	private final ItemRepository items;
	private final CategoryRepository categories;
	private final TagRepository tags;
	private final DiningTableRepository tables;
	private final AreaRepository areas;
	private final UserRepository users;
	private final StringRedisTemplate redis;
	private final JdbcTemplate jdbc;
	private final AiServiceClient ai;

	public ChatService(ChatSessionRepository sessions, ChatMessageRepository messages, ItemRepository items, CategoryRepository categories,
			TagRepository tags, DiningTableRepository tables, AreaRepository areas, UserRepository users, StringRedisTemplate redis, JdbcTemplate jdbc,
			AiServiceClient ai) {
		this.sessions = sessions;
		this.messages = messages;
		this.items = items;
		this.categories = categories;
		this.tags = tags;
		this.tables = tables;
		this.areas = areas;
		this.users = users;
		this.redis = redis;
		this.jdbc = jdbc;
		this.ai = ai;
	}

	@Transactional
	public Map<String, Object> ask(String sessionId, String message) {
		if (message == null || message.isBlank() || message.length() > 500) {
			throw ApiException.badRequest("Câu hỏi từ 1 đến 500 ký tự");
		}
		CurrentUser p = CurrentUser.get();
		rateLimit(p);
		Long owner = p.isCustomer() ? p.userId() : null;
		ChatSession s = sessionId == null ? null : sessions.findById(sessionId).filter(x -> Objects.equals(x.getCustomerId(), owner)).orElse(null);
		if (s == null) { // phiên lạ hoặc của người khác: mở phiên mới
			s = new ChatSession();
			s.setId(UUID.randomUUID().toString());
			s.setCustomerId(owner);
			s.setOrderId(p.orderId());
			s.setTitle(message.length() > 120 ? message.substring(0, 120) : message);
			s = sessions.save(s);
		}
		var req = context(p, s, message);
		Map<String, Object> out;
		try {
			out = ai.chat(req);
		} catch (Exception e) {
			log.warn("AI service lỗi, dùng gợi ý dự phòng: {}", e.getMessage());
			out = fallback(req);
		}
		save(s, "USER", message, List.of(), null, null);
		@SuppressWarnings("unchecked")
		List<Number> ids = (List<Number>) out.getOrDefault("itemIds", List.of());
		Integer latency = out.get("latencyMs") instanceof Number n ? n.intValue() : null;
		save(s, "ASSISTANT", String.valueOf(out.get("reply")), ids, (String) out.get("intent"), latency);
		s.setTurns(s.getTurns() + 1);
		if (latency != null) {
			s.setLatencyMs(latency);
		}
		var res = new HashMap<>(out);
		res.put("sessionId", s.getId());
		return res;
	}

	@Transactional
	public void feedback(String sessionId, int value, boolean addedToCart) {
		sessions.findById(sessionId).ifPresent(s -> {
			s.setFeedback(Integer.signum(value));
			if (addedToCart) {
				s.setAddedToCart(true);
			}
		});
	}

	@Transactional(readOnly = true)
	public List<ChatSessionResponse> sessions() {
		CurrentUser.get().require("ai:view");
		return sessions.findAll().stream().map(ChatSessionResponse::of).toList();
	}

	public Map<String, Object> model() {
		try {
			return ai.health();
		} catch (Exception e) {
			return Map.of("status", "down", "error", "Không kết nối được AI service");
		}
	}

	/** Huấn luyện lại mô hình gợi ý từ lịch sử gọi món đã thanh toán (view v_interactions). */
	@Transactional(readOnly = true)
	public Map<String, Object> retrain() {
		CurrentUser.get().require("ai:manage");
		List<Map<String, Object>> rows = jdbc.queryForList("select customer_id as \"customerId\", item_id as \"itemId\", weight from v_interactions");
		try {
			return ai.retrain(rows);
		} catch (Exception e) {
			throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "AI service không phản hồi");
		}
	}

	private Map<String, Object> context(CurrentUser p, ChatSession s, String message) {
		Map<Long, String> cats = categories.findAll().stream().collect(Collectors.toMap(Category::getId, Category::getName));
		Map<Long, String> tagNames = tags.findAll().stream().collect(Collectors.toMap(Tag::getId, Tag::getName));
		var menu = items.findByDeletedAtIsNullOrderById().stream()
				.map(i -> Map.<String, Object>of("id", i.getId(), "name", i.getName(), "description", Objects.requireNonNullElse(i.getDescription(), ""),
						"price", i.getPrice(), "category", cats.getOrDefault(i.getCategoryId(), ""),
						"tags", i.getTagIds().stream().map(tagNames::get).filter(Objects::nonNull).toList(),
						"spicyLevel", i.getSpicyLevel(), "isAvailable", i.getIsAvailable()))
				.toList();
		List<String> allergy = new ArrayList<>();
		if (p.isCustomer()) {
			users.findById(p.userId()).ifPresent(u -> u.getAllergyTagIds().forEach(t -> allergy.add(tagNames.get(t))));
		}
		Map<Long, String> areaNames = areas.findAll().stream().collect(Collectors.toMap(Area::getId, Area::getName));
		var free = tables.findAll().stream().filter(t -> "AVAILABLE".equals(t.getStatus()) && Boolean.TRUE.equals(t.getIsActive()))
				.sorted((a, b) -> b.getSeats() - a.getSeats()).limit(12) // bàn lớn trước: câu hỏi cho nhóm đông vẫn thấy bàn đủ chỗ
				.map(t -> t.getCode() + " (" + t.getSeats() + " chỗ, " + areaNames.get(t.getAreaId()) + ")").toList();
		var history = messages.findTop8BySessionIdOrderByCreatedAtDesc(s.getId()).reversed().stream()
				.map(m -> Map.<String, Object>of("role", m.getRole(), "content", m.getContent(), "itemIds", ids(m.getItemIds()))).toList();
		var req = new HashMap<String, Object>();
		req.put("message", message);
		req.put("history", history);
		req.put("customerId", p.isCustomer() ? p.userId() : null);
		req.put("allergyTags", allergy);
		req.put("freeTables", free);
		req.put("menu", menu);
		return req;
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> fallback(Map<String, Object> req) {
		var allergy = (List<String>) req.get("allergyTags");
		var picks = ((List<Map<String, Object>>) req.get("menu")).stream()
				.filter(i -> Boolean.TRUE.equals(i.get("isAvailable")) && ((List<String>) i.get("tags")).stream().noneMatch(allergy::contains)).limit(3).toList();
		Function<Map<String, Object>, String> name = i -> (String) i.get("name");
		return Map.of("reply", picks.isEmpty() ? "Trợ lý đang bận, bạn hỏi nhân viên giúp mình nhé."
				: "Trợ lý đang bận, mình gợi ý tạm vài món: " + picks.stream().map(name).collect(Collectors.joining(", ")) + ".",
				"itemIds", picks.stream().map(i -> i.get("id")).toList(), "intent", "KHAC", "llm", "fallback (AI service không phản hồi)");
	}

	private void save(ChatSession s, String role, String content, List<Number> itemIds, String intent, Integer latency) {
		var m = new ChatMessage();
		m.setSessionId(s.getId());
		m.setRole(role);
		m.setContent(content);
		m.setItemIds(itemIds.isEmpty() ? null : itemIds.stream().map(String::valueOf).collect(Collectors.joining(",")));
		m.setIntent(intent);
		m.setLatencyMs(latency);
		messages.save(m);
	}

	private static List<Long> ids(String csv) {
		return csv == null || csv.isBlank() ? List.of() : Arrays.stream(csv.split(",")).map(Long::valueOf).toList();
	}

	private void rateLimit(CurrentUser p) {
		String who = p.userId() != null ? "u" + p.userId() : p.orderId() != null ? "o" + p.orderId() : "anon";
		Long n = redis.opsForValue().increment("rl:chat:" + who);
		if (n != null && n == 1) {
			redis.expire("rl:chat:" + who, Duration.ofMinutes(1));
		}
		if (n != null && n > (who.equals("anon") ? RATE_PER_MINUTE * 5 : RATE_PER_MINUTE)) {
			throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "Bạn hỏi nhanh quá, đợi một phút nhé");
		}
	}

}
