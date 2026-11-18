package com.restaurant.manager.dto.request;

import java.time.Instant;
import java.util.List;

import jakarta.validation.constraints.Size;

/** Đặt bàn. Kiểm tra nghiệp vụ (giờ, số chỗ, trùng lịch, giới hạn) nằm ở ReservationService vì phụ thuộc dữ liệu hiện có. */
public record ReservationRequest(
		@Size(max = 100) String guestName,
		String guestPhone,
		Instant time,
		Integer guestCount,
		List<Long> tableIds,
		@Size(max = 255) String note) {

}
