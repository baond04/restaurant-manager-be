package com.restaurant.manager.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;

import com.restaurant.manager.entity.User;

import jakarta.persistence.LockModeType;

public interface UserRepository extends JpaRepository<User, Long> {

	/** Đọc và khóa dòng đến hết transaction (SELECT ... FOR UPDATE) để hai thao tác đồng thời không ghi đè nhau. */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select x from User x where x.id = ?1")
	Optional<User> lockById(Long id);

	Optional<User> findByEmailIgnoreCase(String email);

	Optional<User> findByPhone(String phone);

	boolean existsByEmailIgnoreCase(String email);

	boolean existsByPhone(String phone);

	boolean existsByRoleId(Long roleId);

}
