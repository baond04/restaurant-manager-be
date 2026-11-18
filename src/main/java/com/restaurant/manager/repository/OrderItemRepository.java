package com.restaurant.manager.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.restaurant.manager.entity.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

	List<OrderItem> findByOrderId(Long orderId);

	boolean existsByOrderIdAndStatusNot(Long orderId, String status);

	@Query("select count(l) > 0 from OrderItem l, CustomerOrder o where l.orderId = o.id and o.customerId = ?1 and l.itemId = ?2 and o.status = 'COMPLETED'")
	boolean customerOrdered(Long customerId, Long itemId);

}
