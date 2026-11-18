package com.restaurant.manager.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.restaurant.manager.entity.Item;

public interface ItemRepository extends JpaRepository<Item, Long> {

	List<Item> findByDeletedAtIsNullOrderById();

	boolean existsByCategoryIdAndDeletedAtIsNull(Long categoryId);

	@Query("select count(i) > 0 from Item i join i.recipe r where r.ingredientId = ?1 and i.deletedAt is null")
	boolean usesIngredient(Long ingredientId);

}
