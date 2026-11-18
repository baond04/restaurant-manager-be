package com.restaurant.manager.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

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

/** Lượt đặt bàn, tự xác nhận khi tạo (không có PENDING). */
@Getter
@Setter
@Entity
@Table(name = "reservations")
public class Reservation extends AuditableEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long customerId;
	private String guestName;
	private String guestPhone;
	private Instant time;
	private Integer guestCount;
	private Long areaId;
	private String note;
	private String status = "CONFIRMED";
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "reservation_tables", joinColumns = @JoinColumn(name = "reservation_id"))
	@Column(name = "table_id")
	private List<Long> tableIds = new ArrayList<>();

}
