package com.restaurant.manager.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.restaurant.manager.entity.OrderItem;
import com.restaurant.manager.entity.Voucher;

/** Cùng các ca với restaurant-manager-fe/src/shared/utils/billing.test.js: hai bên phải tính ra cùng một số. */
class BillCalculatorTest {

	private static final Instant NOW = Instant.parse("2026-11-08T05:00:00Z");

	private static Voucher voucher() {
		var v = new Voucher();
		v.setIsActive(true);
		v.setStartAt(Instant.parse("2026-11-01T00:00:00Z"));
		v.setEndAt(Instant.parse("2026-11-30T00:00:00Z"));
		v.setType("PERCENT");
		v.setValue(10L);
		v.setMaxDiscount(50000L);
		v.setMinOrderAmount(200000L);
		v.setUsageLimit(100);
		v.setUsedCount(0);
		return v;
	}

	private static OrderItem line(int qty, long price, String status) {
		var l = new OrderItem();
		l.setQty(qty);
		l.setUnitPrice(price);
		l.setStatus(status);
		return l;
	}

	@Test
	void percentVoucherIsCappedAndChecksConditions() {
		var v = voucher();
		assertThat(BillCalculator.voucherDiscount(v, 1_000_000, NOW)).isEqualTo(50_000);
		assertThat(BillCalculator.voucherDiscount(v, 100_000, NOW)).isZero();
		v.setUsedCount(100);
		assertThat(BillCalculator.voucherDiscount(v, 1_000_000, NOW)).isZero();
		v.setUsedCount(0);
		v.setEndAt(Instant.parse("2026-11-05T00:00:00Z"));
		assertThat(BillCalculator.voucherDiscount(v, 1_000_000, NOW)).isZero();
	}

	@Test
	void cancelledLinesIgnoredAndVatAdded() {
		var b = BillCalculator.calc(List.of(line(2, 50_000, "ORDERED"), line(1, 90_000, "CANCELLED")), BigDecimal.ZERO, 0, null, 0, NOW);
		assertThat(b.subtotal()).isEqualTo(100_000);
		assertThat(b.tax()).isEqualTo(8_000);
		assertThat(b.total()).isEqualTo(108_000);
		assertThat(b.pointsEarned()).isEqualTo(10);
	}

	@Test
	void vipRoomAddsServiceFeeAndTopsUpToMinSpend() {
		var b = BillCalculator.calc(List.of(line(1, 2_000_000, "ORDERED")), BigDecimal.TEN, 3_000_000, null, 0, NOW);
		assertThat(b.serviceFee()).isEqualTo(200_000);
		assertThat(b.minSpendTopUp()).isEqualTo(1_000_000);
		assertThat(b.total()).isEqualTo(Math.round(3_200_000 * 1.08));
	}

	@Test
	void pointsNeverPushTotalBelowZero() {
		var b = BillCalculator.calc(List.of(line(1, 10_000, "ORDERED")), BigDecimal.ZERO, 0, null, 999, NOW);
		assertThat(b.pointsAmount()).isEqualTo(10_000);
		assertThat(b.total()).isZero();
	}

}
