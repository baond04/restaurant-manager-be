package com.restaurant.manager.exception;

import org.springframework.http.HttpStatus;

/** Lỗi nghiệp vụ có mã HTTP; thông báo bằng tiếng Việt, hiện thẳng cho người dùng. */
public class ApiException extends RuntimeException {

	private final HttpStatus status;

	public ApiException(HttpStatus status, String message) {
		super(message);
		this.status = status;
	}

	public HttpStatus status() {
		return status;
	}

	/** Kiểm giá trị thuộc tập cho phép; null cũng là sai (List.of(...).contains(null) ném NPE nên không dùng trực tiếp). */
	public static String requireOneOf(String value, String message, String... allowed) {
		if (value == null || !java.util.Arrays.asList(allowed).contains(value)) {
			throw badRequest(message);
		}
		return value;
	}

	public static ApiException badRequest(String msg) {
		return new ApiException(HttpStatus.BAD_REQUEST, msg);
	}

	public static ApiException notFound(String msg) {
		return new ApiException(HttpStatus.NOT_FOUND, msg);
	}

	public static ApiException conflict(String msg) {
		return new ApiException(HttpStatus.CONFLICT, msg);
	}

	public static ApiException forbidden() {
		return new ApiException(HttpStatus.FORBIDDEN, "Bạn không có quyền thực hiện thao tác này");
	}

	public static ApiException unauthorized(String msg) {
		return new ApiException(HttpStatus.UNAUTHORIZED, msg);
	}

}
