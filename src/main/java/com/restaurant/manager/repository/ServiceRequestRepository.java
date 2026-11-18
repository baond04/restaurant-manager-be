package com.restaurant.manager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.restaurant.manager.entity.ServiceRequest;

public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, Long> {

	boolean existsByTableIdAndTypeAndStatusNot(Long tableId, String type, String status);

	@Modifying
	@Query("update ServiceRequest s set s.status = 'DONE', s.doneAt = CURRENT_TIMESTAMP where s.orderId = ?1 and s.status <> 'DONE'")
	void closeByOrder(Long orderId);

	@Modifying
	@Query("update ServiceRequest s set s.status = 'DONE', s.doneAt = CURRENT_TIMESTAMP where s.tableId = ?1 and s.type = ?2 and s.status <> 'DONE'")
	void closeByTableAndType(Long tableId, String type);

}
