package com.restaurant.manager.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;

import com.restaurant.manager.entity.DiningTable;

import jakarta.persistence.LockModeType;

public interface DiningTableRepository extends JpaRepository<DiningTable, Long> {

	/** Đọc và khóa dòng đến hết transaction (SELECT ... FOR UPDATE) để hai thao tác đồng thời không ghi đè nhau. */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select x from DiningTable x where x.id = ?1")
	Optional<DiningTable> lockById(Long id);

	Optional<DiningTable> findByQrTokenAndIsActiveTrue(String qrToken);

	boolean existsByCode(String code);

	boolean existsByAreaId(Long areaId);

}
