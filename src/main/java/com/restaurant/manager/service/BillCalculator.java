package com.restaurant.manager.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;

import com.restaurant.manager.entity.OrderItem;
import com.restaurant.manager.entity.Voucher;

/**
 * Tính hóa đơn, cùng công thức với restaurant-manager-fe/src/shared/utils/billing.js (FE chỉ hiển thị tạm tính).
 * Thứ tự: tạm tính → phụ thu khu vực → bù mức chi tối thiểu → voucher → điểm → VAT 8%.
 */
public final class BillCalculator {

	public static final double VAT_RATE = 0.08;
	public static final long POINT_VALUE = 1000; // 1 điểm = 1.000đ
	public static final long POINT_EARN_PER = 10000; // mỗi 10.000đ thanh toán được 1 điểm

	private BillCalculator() {
	}

	public record Result(long subtotal, long serviceFee, long minSpendTopUp, long discount, long pointsAmount, long tax, long total,
			int pointsEarned) {
	}

	/** Giảm giá của voucher trên tạm tính; 0 nếu không đủ điều kiện (hết hạn, hết lượt, chưa đủ mức tối thiểu). */
	public static long voucherDiscount(Voucher v, long subtotal, Instant now) {
		if (v == null || !Boolean.TRUE.equals(v.getIsActive()) || now.isBefore(v.getStartAt()) || now.isAfter(v.getEndAt())) {
			return 0;
		}
		if (v.getUsageLimit() > 0 && v.getUsedCount() >= v.getUsageLimit() || subtotal < v.getMinOrderAmount()) {
			return 0;
		}
		long raw = "PERCENT".equals(v.getType()) ? Math.round(subtotal * v.getValue() / 100.0) : v.getValue();
		long cap = v.getMaxDiscount() > 0 ? v.getMaxDiscount() : raw;
		return Math.min(Math.min(raw, cap), subtotal);
	}

	public static Result calc(List<OrderItem> lines, BigDecimal serviceFeePercent, long minSpend, Voucher voucher, int pointsUsed, Instant now) {
		long subtotal = lines.stream().filter(l -> !"CANCELLED".equals(l.getStatus())).mapToLong(l -> l.getQty() * l.getUnitPrice()).sum();
		long serviceFee = BigDecimal.valueOf(subtotal).multiply(serviceFeePercent).divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP).longValue();
		long topUp = Math.max(0, minSpend - subtotal);
		long discount = voucherDiscount(voucher, subtotal, now);
		long beforePoints = subtotal + serviceFee + topUp - discount;
		long pointsAmount = Math.min(pointsUsed * POINT_VALUE, beforePoints);
		long taxable = beforePoints - pointsAmount;
		long tax = Math.round(taxable * VAT_RATE);
		long total = taxable + tax;
		return new Result(subtotal, serviceFee, topUp, discount, pointsAmount, tax, total, (int) (total / POINT_EARN_PER));
	}

}
