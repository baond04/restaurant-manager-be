package com.restaurant.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Luồng nghiệp vụ chính trên PostgreSQL và Redis thật, dữ liệu demo của hồ sơ dev.
 * Mỗi test dùng bàn riêng để không phụ thuộc thứ tự chạy.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class BusinessFlowTests {

	private static final String STAFF = "linh.do3@nhahang.vn";
	private static final String MANAGER = "quan.le1@nhahang.vn";
	private static final String CUSTOMER = "anhnguyen0@gmail.com";

	@Autowired
	MockMvc mvc;

	@Autowired
	JdbcTemplate jdbc;

	@Autowired
	JsonMapper json;

	private JsonNode call(MockHttpServletRequestBuilder req, String token, Object body, int expected) throws Exception {
		if (token != null) {
			req.header("Authorization", "Bearer " + token);
		}
		if (body != null) {
			req.contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body));
		}
		String out = mvc.perform(req).andExpect(status().is(expected)).andReturn().getResponse().getContentAsString();
		return out.isBlank() ? null : json.readTree(out);
	}

	private String login(String email, String kind) throws Exception {
		return call(post("/api/v1/auth/login"), null, java.util.Map.of("login", email, "password", "123456", "kind", kind), 200)
				.get("accessToken").asString();
	}

	private long tableId(String code) {
		return jdbc.queryForObject("select id from dining_tables where code = ?", Long.class, code);
	}

	@Test
	void publicMenuHidesRecipeAndQrTokens() throws Exception {
		JsonNode items = call(get("/api/v1/items"), null, null, 200);
		assertThat(items.size()).isPositive();
		items.forEach(i -> assertThat(i.get("recipe").size()).isZero());
		call(get("/api/v1/tables"), null, null, 200).forEach(t -> assertThat(t.get("qrToken").isNull()).isTrue());
		assertThat(call(get("/api/v1/tables"), login(STAFF, "STAFF"), null, 200).get(0).get("qrToken").asString()).isNotBlank();
	}

	@Test
	void customersOnlySeeTheirOwnRowsAndCannotEditMenu() throws Exception {
		String c = login(CUSTOMER, "CUSTOMER");
		long me = jdbc.queryForObject("select id from users where email = ?", Long.class, CUSTOMER);
		call(get("/api/v1/orders"), c, null, 200).forEach(o -> assertThat(o.get("customerId").asLong()).isEqualTo(me));
		assertThat(call(get("/api/v1/users"), c, null, 200).size()).isEqualTo(1);
		call(patch("/api/v1/items/1"), c, java.util.Map.of("price", 1), 403);
		call(patch("/api/v1/items/1"), login(STAFF, "STAFF"), java.util.Map.of("price", 1), 403); // nhân viên không có menu:write
		call(patch("/api/v1/users/" + me), c, java.util.Map.of("fullName", "Tên mới"), 200); // tự sửa hồ sơ
		call(patch("/api/v1/users/" + me), c, java.util.Map.of("roleId", 4), 403); // không tự nâng quyền
	}

	@Test
	void qrSessionOrderAndPayment() throws Exception {
		String staff = login(STAFF, "STAFF");
		long t = tableId("A03");
		String qr = jdbc.queryForObject("select qr_token from dining_tables where id = ?", String.class, t);
		call(post("/api/v1/table-sessions/join"), null, java.util.Map.of("qrToken", qr), 409); // bàn chưa mở
		assertThat(call(get("/api/v1/table-sessions/table?qrToken=" + qr), null, null, 200).get("code").asString()).isEqualTo("A03");
		call(get("/api/v1/table-sessions/table?qrToken=khong-ton-tai"), null, null, 404);
		long order = call(post("/api/v1/tables/" + t + "/open"), staff, java.util.Map.of("guestCount", 2), 200).get("id").asLong();
		String guest = call(post("/api/v1/table-sessions/join"), null, java.util.Map.of("qrToken", qr), 200).get("token").asString();

		long soldOut = jdbc.queryForObject("select id from items where is_available = false limit 1", Long.class);
		call(post("/api/v1/orders/items"), guest, java.util.Map.of("lines", java.util.List.of(java.util.Map.of("itemId", soldOut, "qty", 1))), 409);
		call(post("/api/v1/orders/items"), guest, java.util.Map.of("lines", java.util.List.of(java.util.Map.of("itemId", 6, "qty", 2))), 200);
		assertThat(call(get("/api/v1/order-items"), guest, null, 200).size()).isEqualTo(1); // chỉ thấy món của bàn mình

		call(post("/api/v1/orders/" + order + "/pay"), guest, java.util.Map.of("method", "CASH"), 403); // khách không tự thu tiền
		long price = jdbc.queryForObject("select price from items where id = 6", Long.class);
		JsonNode bill = call(post("/api/v1/orders/" + order + "/pay"), staff, java.util.Map.of("method", "CASH"), 200);
		assertThat(bill.get("total").asLong()).isEqualTo(Math.round(2 * price * 1.08));
		call(post("/api/v1/orders/" + order + "/pay"), staff, java.util.Map.of("method", "CASH"), 409); // thu hai lần
		call(post("/api/v1/orders/items"), guest, java.util.Map.of("lines", java.util.List.of(java.util.Map.of("itemId", 6, "qty", 1))), 409); // đơn đã đóng
		call(post("/api/v1/bills/" + bill.get("id").asLong() + "/void"), staff, null, 403); // chỉ quản lý hủy hóa đơn
		call(post("/api/v1/bills/" + bill.get("id").asLong() + "/void"), login(MANAGER, "STAFF"), null, 200);
	}

	@Test
	void emptyOrderCannotBePaid() throws Exception {
		String staff = login(STAFF, "STAFF");
		long order = call(post("/api/v1/tables/" + tableId("A08") + "/open"), staff, java.util.Map.of("guestCount", 1), 200).get("id").asLong();
		call(post("/api/v1/orders/" + order + "/pay"), staff, java.util.Map.of("method", "CASH"), 400);
		call(post("/api/v1/orders/" + order + "/pay"), staff, java.util.Map.of(), 400); // thiếu phương thức: 400, không phải 500
	}

	@Test
	void reservationOverlapIsRejected() throws Exception {
		long t = tableId("B03");
		String time = Instant.now().plus(3, ChronoUnit.DAYS).truncatedTo(ChronoUnit.HOURS).toString();
		var req = java.util.Map.of("guestName", "Khách thử", "guestPhone", "0911111111", "time", time, "guestCount", 2, "tableIds", java.util.List.of(t));
		call(post("/api/v1/reservations"), null, req, 201).get("status").asString().equals("CONFIRMED");
		var other = new java.util.HashMap<>(req);
		other.put("guestPhone", "0922222222");
		other.put("time", Instant.parse(time).plus(90, ChronoUnit.MINUTES).toString()); // trong ±2 giờ
		call(post("/api/v1/reservations"), null, other, 409);
		var samePhone = new java.util.HashMap<>(req);
		samePhone.put("tableIds", java.util.List.of(tableId("B04")));
		call(post("/api/v1/reservations"), null, samePhone, 429); // bàn khác nhưng cùng số điện thoại: chỉ giữ một lượt
		other.put("time", Instant.parse(time).plus(3, ChronoUnit.HOURS).toString());
		long second = call(post("/api/v1/reservations"), null, other, 201).get("id").asLong();

		String staff = login(STAFF, "STAFF");
		var moved = new java.util.HashMap<>(other);
		moved.put("time", Instant.parse(time).plus(150, ChronoUnit.MINUTES).toString()); // chỉ trùng chính nó: được sửa
		assertThat(call(patch("/api/v1/reservations/" + second), staff, moved, 200).get("time").asString()).isEqualTo(moved.get("time"));
		moved.put("time", time); // trùng lượt đầu
		call(patch("/api/v1/reservations/" + second), staff, moved, 409);
		call(patch("/api/v1/reservations/" + second), login(CUSTOMER, "CUSTOMER"), other, 403);
	}

	@Test
	void loginLocksAfterFiveFailures() throws Exception {
		var bad = java.util.Map.of("login", "chau.ngo4@nhahang.vn", "password", "sai", "kind", "STAFF");
		for (int i = 0; i < 5; i++) {
			call(post("/api/v1/auth/login"), null, bad, 401);
		}
		call(post("/api/v1/auth/login"), null, java.util.Map.of("login", "chau.ngo4@nhahang.vn", "password", "123456", "kind", "STAFF"), 429);
	}

	@Test
	void auditFieldsRecordWhoAndWhen() throws Exception {
		String manager = login(MANAGER, "STAFF");
		long me = jdbc.queryForObject("select id from users where email = ?", Long.class, MANAGER);
		JsonNode created = call(post("/api/v1/categories"), manager, java.util.Map.of("name", "Món thử audit"), 201);
		assertThat(created.get("createdBy").asLong()).isEqualTo(me);
		assertThat(created.get("createdAt").isNull()).isFalse();
		JsonNode updated = call(patch("/api/v1/categories/" + created.get("id").asLong()), manager, java.util.Map.of("sortOrder", 99), 200);
		assertThat(updated.get("updatedBy").asLong()).isEqualTo(me);
		assertThat(updated.get("name").asString()).isEqualTo("Món thử audit"); // sửa từng phần không xóa trường khác
		call(post("/api/v1/categories"), manager, java.util.Map.of("sortOrder", 1), 400); // thiếu tên khi tạo
	}

	@Test
	void kitchenStaffWithoutAccountCannotLogIn() throws Exception {
		String chef = jdbc.queryForObject("select email from users where position = 'Bếp trưởng'", String.class);
		call(post("/api/v1/auth/login"), null, java.util.Map.of("login", chef, "password", "123456", "kind", "STAFF"), 401);
	}

}
