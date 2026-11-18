package com.restaurant.manager.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.restaurant.manager.dto.response.ChatSessionResponse;
import com.restaurant.manager.service.ChatService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Trợ lý AI")
@RestController
@RequestMapping("/api/v1")
public class ChatController {

	private final ChatService chat;

	public ChatController(ChatService chat) {
		this.chat = chat;
	}

	public record AskRequest(String sessionId, String message) {
	}

	public record FeedbackRequest(Integer value, Boolean addedToCart) {
	}

	@Operation(summary = "Gửi một câu cho trợ lý; trả câu trả lời và id các món được gợi ý")
	@PostMapping("/chat")
	public Map<String, Object> ask(@RequestBody AskRequest req) {
		return chat.ask(req.sessionId(), req.message());
	}

	@PostMapping("/chat/{sessionId}/feedback")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void feedback(@PathVariable String sessionId, @RequestBody FeedbackRequest req) {
		chat.feedback(sessionId, req.value() == null ? 0 : req.value(), Boolean.TRUE.equals(req.addedToCart()));
	}

	@GetMapping("/chat-sessions")
	public List<ChatSessionResponse> sessions() {
		return chat.sessions();
	}

	@Operation(summary = "Tình trạng AI service và mô hình gợi ý")
	@GetMapping("/ai/model")
	public Map<String, Object> model() {
		return chat.model();
	}

	@Operation(summary = "Huấn luyện lại mô hình gợi ý từ lịch sử gọi món")
	@PostMapping("/ai/retrain")
	public Map<String, Object> retrain() {
		return chat.retrain();
	}

}
