package com.restaurant.manager.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.restaurant.manager.dto.request.OnCreate;
import com.restaurant.manager.dto.request.ReviewRequest;
import com.restaurant.manager.dto.response.ReviewResponse;
import com.restaurant.manager.service.ReviewService;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Đánh giá món")
@RestController
@RequestMapping("/api/v1/reviews")
public class ReviewController {

	private final ReviewService reviews;

	public ReviewController(ReviewService reviews) {
		this.reviews = reviews;
	}

	@GetMapping
	public List<ReviewResponse> list() {
		return reviews.list();
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ReviewResponse create(@Validated(OnCreate.class) @RequestBody ReviewRequest req) {
		return reviews.create(req);
	}

	@PatchMapping("/{id}/visibility")
	public ReviewResponse visibility(@PathVariable long id, @RequestBody Map<String, Boolean> body) {
		return reviews.setHidden(id, Boolean.TRUE.equals(body.get("hidden")));
	}

}
