package com.restaurant.manager.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.restaurant.manager.dto.request.OnCreate;
import com.restaurant.manager.dto.request.VoucherRequest;
import com.restaurant.manager.dto.response.VoucherResponse;
import com.restaurant.manager.service.VoucherService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Voucher")
@RestController
@RequestMapping("/api/v1/vouchers")
public class VoucherController {

	private final VoucherService vouchers;

	public VoucherController(VoucherService vouchers) {
		this.vouchers = vouchers;
	}

	@GetMapping
	public List<VoucherResponse> list() {
		return vouchers.list();
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public VoucherResponse create(@Validated(OnCreate.class) @RequestBody VoucherRequest req) {
		return vouchers.create(req);
	}

	@PatchMapping("/{id}")
	public VoucherResponse update(@PathVariable long id, @Valid @RequestBody VoucherRequest req) {
		return vouchers.update(id, req);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable long id) {
		vouchers.delete(id);
	}

}
