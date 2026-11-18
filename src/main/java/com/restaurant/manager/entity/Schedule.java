package com.restaurant.manager.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;

/** Lịch làm: nhân viên, ca, ngày, khu vực phụ trách. */
@Getter
@Setter
@Entity
@Table(name = "schedules")
public class Schedule extends AuditableEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long employeeId;
	private Long shiftId;
	private Long areaId;
	@Column(name = "work_date")
	private LocalDate date;
	private String status = "SCHEDULED";

}
