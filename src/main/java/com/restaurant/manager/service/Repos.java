package com.restaurant.manager.service;

import org.springframework.data.jpa.repository.JpaRepository;

import com.restaurant.manager.exception.ApiException;

/** Tìm theo id, không có thì 404 với thông báo tiếng Việt. */
final class Repos {

	private Repos() {
	}

	static <T, ID> T find(JpaRepository<T, ID> repo, ID id) {
		if (id == null) {
			throw ApiException.badRequest("Thiếu mã dữ liệu");
		}
		return repo.findById(id).orElseThrow(() -> ApiException.notFound("Không tìm thấy dữ liệu"));
	}

}
