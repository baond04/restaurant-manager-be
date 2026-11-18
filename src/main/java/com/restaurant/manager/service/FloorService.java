package com.restaurant.manager.service;

import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restaurant.manager.dto.request.AreaRequest;
import com.restaurant.manager.dto.request.DiningTableRequest;
import com.restaurant.manager.dto.request.FloorRequest;
import com.restaurant.manager.dto.response.AreaResponse;
import com.restaurant.manager.dto.response.DiningTableResponse;
import com.restaurant.manager.dto.response.FloorResponse;
import com.restaurant.manager.entity.Area;
import com.restaurant.manager.entity.DiningTable;
import com.restaurant.manager.entity.Floor;
import com.restaurant.manager.exception.ApiException;
import com.restaurant.manager.repository.AreaRepository;
import com.restaurant.manager.repository.DiningTableRepository;
import com.restaurant.manager.repository.FloorRepository;
import com.restaurant.manager.repository.OrderRepository;
import com.restaurant.manager.security.CurrentUser;

/** Tầng, khu vực, bàn. Sơ đồ bàn công khai để khách đặt bàn; mã QR chỉ nhân viên thấy vì lộ mã là gọi món từ xa được. */
@Service
public class FloorService {

	private static final String MANAGE = "floor:manage";
	private static final SecureRandom RANDOM = new SecureRandom();

	private final FloorRepository floors;
	private final AreaRepository areas;
	private final DiningTableRepository tables;
	private final OrderRepository orders;

	public FloorService(FloorRepository floors, AreaRepository areas, DiningTableRepository tables, OrderRepository orders) {
		this.floors = floors;
		this.areas = areas;
		this.tables = tables;
		this.orders = orders;
	}

	@Transactional(readOnly = true)
	public List<FloorResponse> floors() {
		return floors.findAll().stream().map(FloorResponse::of).toList();
	}

	@Transactional
	public FloorResponse createFloor(FloorRequest req) {
		CurrentUser.get().require(MANAGE);
		var e = new Floor();
		req.applyTo(e);
		return FloorResponse.of(floors.save(e));
	}

	@Transactional
	public FloorResponse updateFloor(long id, FloorRequest req) {
		CurrentUser.get().require(MANAGE);
		Floor e = Repos.find(floors, id);
		req.applyTo(e);
		return FloorResponse.of(e);
	}

	@Transactional
	public void deleteFloor(long id) {
		CurrentUser.get().require(MANAGE);
		if (areas.existsByFloorId(id)) {
			throw ApiException.conflict("Tầng còn khu vực. Xóa khu vực trước.");
		}
		floors.delete(Repos.find(floors, id));
	}

	@Transactional(readOnly = true)
	public List<AreaResponse> areas() {
		return areas.findAll().stream().map(AreaResponse::of).toList();
	}

	@Transactional
	public AreaResponse createArea(AreaRequest req) {
		CurrentUser.get().require(MANAGE);
		Repos.find(floors, req.floorId());
		var e = new Area();
		req.applyTo(e);
		return AreaResponse.of(areas.save(e));
	}

	@Transactional
	public AreaResponse updateArea(long id, AreaRequest req) {
		CurrentUser.get().require(MANAGE);
		Area e = Repos.find(areas, id);
		req.applyTo(e);
		return AreaResponse.of(e);
	}

	@Transactional
	public void deleteArea(long id) {
		CurrentUser.get().require(MANAGE);
		if (tables.existsByAreaId(id)) {
			throw ApiException.conflict("Khu vực còn bàn. Xóa hoặc chuyển bàn trước.");
		}
		areas.delete(Repos.find(areas, id));
	}

	@Transactional(readOnly = true)
	public List<DiningTableResponse> tables() {
		boolean staff = CurrentUser.get().canAny(Set.of("table:view", MANAGE));
		return tables.findAll().stream().map(DiningTableResponse::of).map(t -> staff ? t : hideQr(t)).toList();
	}

	@Transactional
	public DiningTableResponse createTable(DiningTableRequest req) {
		CurrentUser.get().require(MANAGE);
		if (tables.existsByCode(req.code())) {
			throw ApiException.conflict("Mã bàn " + req.code() + " đã tồn tại");
		}
		var e = new DiningTable();
		req.applyTo(e);
		e.setQrToken(newToken());
		return DiningTableResponse.of(tables.save(e));
	}

	@Transactional
	public DiningTableResponse updateTable(long id, DiningTableRequest req) {
		CurrentUser.get().require(MANAGE);
		DiningTable e = Repos.find(tables, id);
		req.applyTo(e);
		return DiningTableResponse.of(e);
	}

	@Transactional
	public void deleteTable(long id) {
		CurrentUser.get().require(MANAGE);
		if (orders.existsByTableId(id)) {
			throw ApiException.conflict("Bàn đã có đơn. Chỉ ngừng dùng được.");
		}
		tables.delete(Repos.find(tables, id));
	}

	/** Thêm nhiều bàn một lần: tiền tố A, từ 1, 6 bàn → A01..A06. */
	@Transactional
	public List<String> bulkAdd(long areaId, String prefix, int from, int count, int seats) {
		CurrentUser.get().require(MANAGE);
		if (count < 1 || count > 50 || seats < 1 || prefix == null || prefix.isBlank()) {
			throw ApiException.badRequest("Cần tiền tố, số bàn từ 1 đến 50, số chỗ lớn hơn 0");
		}
		Repos.find(areas, areaId);
		var codes = IntStream.range(from, from + count).mapToObj(i -> prefix + String.format("%02d", i)).toList();
		codes.stream().filter(tables::existsByCode).findFirst().ifPresent(c -> {
			throw ApiException.conflict("Mã bàn " + c + " đã tồn tại");
		});
		for (String code : codes) {
			var t = new DiningTable();
			t.setAreaId(areaId);
			t.setCode(code);
			t.setSeats(seats);
			t.setShape(seats <= 2 ? "ROUND" : seats <= 4 ? "SQUARE" : "RECT");
			t.setQrToken(newToken());
			tables.save(t);
		}
		return codes;
	}

	/** Đổi mã QR: mã cũ hết hiệu lực ngay (ví dụ khi ảnh QR bị chụp mang ra ngoài). */
	@Transactional
	public void regenerateQr(long tableId) {
		CurrentUser.get().require(MANAGE);
		tables.lockById(tableId).orElseThrow(() -> ApiException.notFound("Không tìm thấy bàn")).setQrToken(newToken());
	}

	@Transactional
	public void setStatus(long tableId, String status) {
		CurrentUser.get().require("table:operate");
		ApiException.requireOneOf(status, "Trạng thái không hợp lệ: " + status, "AVAILABLE", "CLEANING", "DISABLED");
		DiningTable t = tables.lockById(tableId).orElseThrow(() -> ApiException.notFound("Không tìm thấy bàn"));
		if ("OCCUPIED".equals(t.getStatus())) {
			throw ApiException.conflict("Bàn đang có khách, hãy thanh toán hoặc chuyển bàn trước");
		}
		t.setStatus(status);
	}

	private static DiningTableResponse hideQr(DiningTableResponse t) {
		return new DiningTableResponse(t.id(), t.areaId(), t.code(), t.seats(), t.status(), t.isActive(), null, t.shape(), t.posX(), t.posY(),
				t.createdAt(), t.createdBy(), t.updatedAt(), t.updatedBy());
	}

	static String newToken() {
		byte[] b = new byte[12];
		RANDOM.nextBytes(b);
		return HexFormat.of().formatHex(b); // 24 ký tự hex, không đoán được
	}

}
