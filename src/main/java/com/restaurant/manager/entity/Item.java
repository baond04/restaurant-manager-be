package com.restaurant.manager.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;

/** Món ăn. isAvailable = false nghĩa là hết món: không gọi được, trợ lý không gợi ý. */
@Getter
@Setter
@Entity
@Table(name = "items")
public class Item extends AuditableEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long categoryId;
	private String name;
	private String description;
	private Long price;
	private String image;
	private Boolean isAvailable = true;
	private Integer spicyLevel = 0;
	private Integer prepMinutes = 10;
	private Instant deletedAt;
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "item_tags", joinColumns = @JoinColumn(name = "item_id"))
	@Column(name = "tag_id")
	private Set<Long> tagIds = new HashSet<>();
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "item_recipe", joinColumns = @JoinColumn(name = "item_id"))
	private List<RecipeLine> recipe = new ArrayList<>();

}
