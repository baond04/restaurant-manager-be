package com.restaurant.manager.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;

import com.restaurant.manager.entity.Order;

import jakarta.persistence.LockModeType;

public interface OrderRepository extends JpaRepository<Order, Long> {

	/** Đọc và khóa dòng đến hết transaction (SELECT ... FOR UPDATE) để hai thao tác đồng thời không ghi đè nhau. */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select x from CustomerOrder x where x.id = ?1")
	Optional<Order> lockById(Long id);

	Optional<Order> findFirstByTableIdAndStatus(Long tableId, String status);

	boolean existsByTableId(Long tableId);

	boolean existsByOpenedByOrCustomerId(Long openedBy, Long customerId);

}
