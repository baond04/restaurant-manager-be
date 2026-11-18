package com.restaurant.manager.entity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

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

/** Tài khoản nhân viên hoặc khách. Nhân viên không có role_id (bếp, phục vụ) không đăng nhập được. */
@Getter
@Setter
@Entity
@Table(name = "users")
public class User extends AuditableEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String kind;
	private Long roleId;
	private String fullName;
	private String position;
	private String email;
	private String phone;
	@JsonIgnore
	private String passwordHash;
	private String status = "ACTIVE";
	private String workStatus;
	private LocalDate hireDate;
	private String gender;
	private Integer points = 0;
	private Instant lastLoginAt;
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "user_like_tags", joinColumns = @JoinColumn(name = "user_id"))
	@Column(name = "tag_id")
	private Set<Long> likeTagIds = new HashSet<>();
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "user_allergy_tags", joinColumns = @JoinColumn(name = "user_id"))
	@Column(name = "tag_id")
	private Set<Long> allergyTagIds = new HashSet<>();

}
