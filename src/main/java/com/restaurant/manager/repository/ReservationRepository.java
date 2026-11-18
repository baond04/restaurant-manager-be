package com.restaurant.manager.repository;

import java.util.Optional;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import com.restaurant.manager.entity.Reservation;

import jakarta.persistence.LockModeType;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

	/** Đọc và khóa dòng đến hết transaction (SELECT ... FOR UPDATE) để hai thao tác đồng thời không ghi đè nhau. */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select x from Reservation x where x.id = ?1")
	Optional<Reservation> lockById(Long id);

	@Query("select count(r) > 0 from Reservation r join r.tableIds t where t = ?1 and r.status in ('CONFIRMED', 'SEATED') and r.time > ?2 and r.time < ?3 and r.id <> ?4")
	boolean tableBusy(Long tableId, Instant from, Instant to, Long exceptReservationId);

	long countByCustomerIdAndStatusAndTimeAfter(Long customerId, String status, Instant time);

	long countByGuestPhoneAndStatusAndTimeAfter(String phone, String status, Instant time);

	List<Reservation> findByStatusInAndTimeBetween(Collection<String> statuses, Instant from, Instant to);

}
