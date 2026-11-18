package com.restaurant.manager.service;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restaurant.manager.dto.request.VoucherRequest;
import com.restaurant.manager.dto.response.VoucherResponse;
import com.restaurant.manager.entity.Voucher;
import com.restaurant.manager.exception.ApiException;
import com.restaurant.manager.repository.VoucherRepository;
import com.restaurant.manager.security.CurrentUser;

/** Voucher. Thu ngân xem để áp khi thanh toán; tạo, sửa cần voucher:manage. Voucher đã dùng chỉ tắt được, không xóa. */
@Service
public class VoucherService {

	private final VoucherRepository vouchers;

	public VoucherService(VoucherRepository vouchers) {
		this.vouchers = vouchers;
	}

	@Transactional(readOnly = true)
	public List<VoucherResponse> list() {
		CurrentUser.get().requireAny(Set.of("voucher:manage", "bill:discount", "report:view"));
		return vouchers.findAll().stream().map(VoucherResponse::of).toList();
	}

	@Transactional
	public VoucherResponse create(VoucherRequest req) {
		CurrentUser.get().require("voucher:manage");
		validate(req.startAt(), req.endAt());
		var e = new Voucher();
		req.applyTo(e);
		return VoucherResponse.of(vouchers.save(e));
	}

	@Transactional
	public VoucherResponse update(long id, VoucherRequest req) {
		CurrentUser.get().require("voucher:manage");
		Voucher e = Repos.find(vouchers, id);
		req.applyTo(e);
		validate(e.getStartAt(), e.getEndAt());
		return VoucherResponse.of(e);
	}

	@Transactional
	public void delete(long id) {
		CurrentUser.get().require("voucher:manage");
		Voucher v = Repos.find(vouchers, id);
		if (v.getUsedCount() > 0) {
			throw ApiException.conflict("Voucher đã được dùng. Chỉ tắt được.");
		}
		vouchers.delete(v);
	}

	private static void validate(java.time.Instant start, java.time.Instant end) {
		if (start != null && end != null && !end.isAfter(start)) {
			throw ApiException.badRequest("Ngày kết thúc phải sau ngày bắt đầu");
		}
	}

}
