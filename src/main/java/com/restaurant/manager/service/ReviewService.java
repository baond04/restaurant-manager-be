package com.restaurant.manager.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restaurant.manager.dto.request.ReviewRequest;
import com.restaurant.manager.dto.response.ReviewResponse;
import com.restaurant.manager.entity.Review;
import com.restaurant.manager.exception.ApiException;
import com.restaurant.manager.repository.OrderItemRepository;
import com.restaurant.manager.repository.ReviewRepository;
import com.restaurant.manager.security.CurrentUser;

/** Đánh giá món: khách chỉ đánh giá món đã gọi và thanh toán; quản lý ẩn được đánh giá vi phạm. */
@Service
public class ReviewService {

	private final ReviewRepository reviews;
	private final OrderItemRepository orderItems;

	public ReviewService(ReviewRepository reviews, OrderItemRepository orderItems) {
		this.reviews = reviews;
		this.orderItems = orderItems;
	}

	@Transactional(readOnly = true)
	public List<ReviewResponse> list() {
		boolean moderator = CurrentUser.get().can("review:moderate");
		return reviews.findAll().stream().filter(r -> moderator || !Boolean.TRUE.equals(r.getHidden())).map(ReviewResponse::of).toList();
	}

	@Transactional
	public ReviewResponse create(ReviewRequest req) {
		CurrentUser p = CurrentUser.get();
		if (!p.isCustomer()) {
			throw ApiException.forbidden();
		}
		if (!orderItems.customerOrdered(p.userId(), req.itemId())) {
			throw ApiException.badRequest("Chỉ đánh giá được món bạn đã gọi và thanh toán");
		}
		var e = new Review();
		req.applyTo(e);
		e.setCustomerId(p.userId());
		e.setHidden(false);
		return ReviewResponse.of(reviews.save(e));
	}

	/** Quản lý chỉ đổi cờ ẩn, không sửa nội dung của khách. */
	@Transactional
	public ReviewResponse setHidden(long id, boolean hidden) {
		CurrentUser.get().require("review:moderate");
		Review e = Repos.find(reviews, id);
		e.setHidden(hidden);
		return ReviewResponse.of(e);
	}

}
