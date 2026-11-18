package com.restaurant.manager.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Mọi lỗi trả theo RFC 7807 (ProblemDetail); FE đọc trường detail để hiện thông báo. */
@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(ApiException.class)
	ProblemDetail api(ApiException e) {
		return ProblemDetail.forStatusAndDetail(e.status(), e.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ProblemDetail invalid(MethodArgumentNotValidException e) {
		var first = e.getBindingResult().getFieldErrors().stream().findFirst();
		return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
				first.map(f -> f.getField() + ": " + f.getDefaultMessage()).orElse("Dữ liệu không hợp lệ"));
	}

	@ExceptionHandler(AccessDeniedException.class)
	ProblemDetail denied() {
		return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, "Bạn không có quyền thực hiện thao tác này");
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	ProblemDetail integrity(DataIntegrityViolationException e) {
		log.warn("Vi phạm ràng buộc dữ liệu: {}", e.getMostSpecificCause().getMessage());
		return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "Dữ liệu trùng hoặc đang được dùng ở nơi khác");
	}

}
