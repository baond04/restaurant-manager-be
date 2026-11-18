package com.restaurant.manager.service;

import java.util.List;
import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restaurant.manager.dto.request.UserRequest;
import com.restaurant.manager.dto.response.UserResponse;
import com.restaurant.manager.entity.User;
import com.restaurant.manager.exception.ApiException;
import com.restaurant.manager.repository.OrderRepository;
import com.restaurant.manager.repository.UserRepository;
import com.restaurant.manager.security.CurrentUser;

/**
 * Tài khoản nhân viên và khách. Nhân viên vận hành xem được danh sách (tra khách theo số điện thoại, xem ai thu tiền);
 * khách chỉ thấy và tự sửa hồ sơ của mình; đổi vai trò hoặc khóa tài khoản cần user:manage.
 */
@Service
public class UserService {

	private static final Set<String> VIEW = Set.of("user:manage", "staff:view", "customer:view", "order:view", "bill:view", "dashboard:view", "report:view");
	/** Trường khách được tự sửa trong hồ sơ. */
	private static final Set<String> SELF = Set.of("fullName", "phone", "gender", "likeTagIds", "allergyTagIds", "password");

	private final UserRepository users;
	private final OrderRepository orders;
	private final PasswordEncoder encoder;

	public UserService(UserRepository users, OrderRepository orders, PasswordEncoder encoder) {
		this.users = users;
		this.orders = orders;
		this.encoder = encoder;
	}

	@Transactional(readOnly = true)
	public List<UserResponse> list() {
		CurrentUser p = CurrentUser.get();
		if (p.isStaff() && p.canAny(VIEW)) {
			return users.findAll().stream().map(UserResponse::of).toList();
		}
		if (p.userId() == null) {
			throw ApiException.forbidden();
		}
		return List.of(UserResponse.of(Repos.find(users, p.userId())));
	}

	@Transactional
	public UserResponse create(UserRequest req) {
		CurrentUser.get().require("user:manage");
		checkUnique(req, null);
		var e = new User();
		req.applyTo(e);
		if (req.password() != null) {
			e.setPasswordHash(encoder.encode(req.password()));
		}
		return UserResponse.of(users.save(e));
	}

	@Transactional
	public UserResponse update(long id, UserRequest req) {
		CurrentUser p = CurrentUser.get();
		boolean self = Long.valueOf(id).equals(p.userId()) && SELF.containsAll(sentFields(req));
		if (!self) {
			p.require("user:manage");
		}
		User e = Repos.find(users, id);
		checkUnique(req, id);
		req.applyTo(e);
		if (req.password() != null) {
			e.setPasswordHash(encoder.encode(req.password()));
		}
		return UserResponse.of(e);
	}

	@Transactional
	public void delete(long id) {
		CurrentUser.get().require("user:manage");
		if (orders.existsByOpenedByOrCustomerId(id, id)) {
			throw ApiException.conflict("Tài khoản đã có dữ liệu. Hãy khóa thay vì xóa.");
		}
		users.delete(Repos.find(users, id));
	}

	/** Tên các trường client gửi lên (khác null), để biết khách có sửa trường ngoài hồ sơ của mình không. */
	private static Set<String> sentFields(UserRequest req) {
		var out = new java.util.HashSet<String>();
		for (var c : UserRequest.class.getRecordComponents()) {
			try {
				if (c.getAccessor().invoke(req) != null) {
					out.add(c.getName());
				}
			} catch (ReflectiveOperationException e) {
				throw new IllegalStateException(e);
			}
		}
		return out;
	}

	private void checkUnique(UserRequest req, Long id) {
		if (req.email() != null && users.findByEmailIgnoreCase(req.email()).filter(u -> !u.getId().equals(id)).isPresent()) {
			throw ApiException.conflict("Email đã được dùng");
		}
		if (req.phone() != null && users.findByPhone(req.phone()).filter(u -> !u.getId().equals(id)).isPresent()) {
			throw ApiException.conflict("Số điện thoại đã được dùng");
		}
	}

}
