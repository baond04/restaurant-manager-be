package com.restaurant.manager.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.restaurant.manager.entity.InventoryTransaction;

public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {

}
