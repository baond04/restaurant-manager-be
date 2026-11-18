package com.restaurant.manager.dto.request;


import com.restaurant.manager.entity.Review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Dữ liệu client gửi để tạo hoặc sửa Review. Trường null khi sửa nghĩa là giữ nguyên (sửa từng phần). */
public record ReviewRequest(
		@NotNull(groups = OnCreate.class) Long itemId,
		@NotNull(groups = OnCreate.class) @Min(1) @Max(5) Integer rating,
		@Size(max = 1000) String comment,
		Boolean hidden) {

	/** Chép các trường khác null vào entity. */
	public void applyTo(Review e) {
		if (itemId != null) {
			e.setItemId(itemId);
		}
		if (rating != null) {
			e.setRating(rating);
		}
		if (comment != null) {
			e.setComment(comment);
		}
		if (hidden != null) {
			e.setHidden(hidden);
		}
	}

}
