package com.restaurant.manager.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;

import com.restaurant.manager.entity.Voucher;

import jakarta.persistence.LockModeType;

public interface VoucherRepository extends JpaRepository<Voucher, Long> {

	/** Đọc và khóa dòng đến hết transaction (SELECT ... FOR UPDATE) để hai thao tác đồng thời không ghi đè nhau. */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select x from Voucher x where x.id = ?1")
	Optional<Voucher> lockById(Long id);

}
