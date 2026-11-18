package com.restaurant.manager.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.restaurant.manager.dto.request.AreaRequest;
import com.restaurant.manager.dto.request.DiningTableRequest;
import com.restaurant.manager.dto.request.FloorRequest;
import com.restaurant.manager.dto.request.OnCreate;
import com.restaurant.manager.dto.response.AreaResponse;
import com.restaurant.manager.dto.response.DiningTableResponse;
import com.restaurant.manager.dto.response.FloorResponse;
import com.restaurant.manager.service.FloorService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Tầng, khu vực, bàn")
@RestController
@RequestMapping("/api/v1")
public class FloorController {

	private final FloorService floors;

	public FloorController(FloorService floors) {
		this.floors = floors;
	}

	public record BulkTablesRequest(Long areaId, String prefix, Integer from, Integer count, Integer seats) {
	}

	@GetMapping("/floors")
	public List<FloorResponse> floors() {
		return floors.floors();
	}

	@PostMapping("/floors")
	@ResponseStatus(HttpStatus.CREATED)
	public FloorResponse createFloor(@Validated(OnCreate.class) @RequestBody FloorRequest req) {
		return floors.createFloor(req);
	}

	@PatchMapping("/floors/{id}")
	public FloorResponse updateFloor(@PathVariable long id, @Valid @RequestBody FloorRequest req) {
		return floors.updateFloor(id, req);
	}

	@DeleteMapping("/floors/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteFloor(@PathVariable long id) {
		floors.deleteFloor(id);
	}

	@GetMapping("/areas")
	public List<AreaResponse> areas() {
		return floors.areas();
	}

	@PostMapping("/areas")
	@ResponseStatus(HttpStatus.CREATED)
	public AreaResponse createArea(@Validated(OnCreate.class) @RequestBody AreaRequest req) {
		return floors.createArea(req);
	}

	@PatchMapping("/areas/{id}")
	public AreaResponse updateArea(@PathVariable long id, @Valid @RequestBody AreaRequest req) {
		return floors.updateArea(id, req);
	}

	@DeleteMapping("/areas/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteArea(@PathVariable long id) {
		floors.deleteArea(id);
	}

	@Operation(summary = "Danh sách bàn; mã QR chỉ hiện với nhân viên")
	@GetMapping("/tables")
	public List<DiningTableResponse> tables() {
		return floors.tables();
	}

	@PostMapping("/tables")
	@ResponseStatus(HttpStatus.CREATED)
	public DiningTableResponse createTable(@Validated(OnCreate.class) @RequestBody DiningTableRequest req) {
		return floors.createTable(req);
	}

	@PatchMapping("/tables/{id}")
	public DiningTableResponse updateTable(@PathVariable long id, @Valid @RequestBody DiningTableRequest req) {
		return floors.updateTable(id, req);
	}

	@DeleteMapping("/tables/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteTable(@PathVariable long id) {
		floors.deleteTable(id);
	}

	@Operation(summary = "Thêm nhiều bàn: tiền tố A, từ 1, 6 bàn → A01..A06")
	@PostMapping("/tables/bulk")
	@ResponseStatus(HttpStatus.CREATED)
	public List<String> bulk(@RequestBody BulkTablesRequest req) {
		return floors.bulkAdd(req.areaId(), req.prefix(), req.from() == null ? 1 : req.from(), req.count() == null ? 0 : req.count(),
				req.seats() == null ? 0 : req.seats());
	}

	@Operation(summary = "Đổi mã QR của bàn, mã cũ hết hiệu lực")
	@PostMapping("/tables/{id}/regenerate-qr")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void regenerateQr(@PathVariable long id) {
		floors.regenerateQr(id);
	}

	@Operation(summary = "Đổi trạng thái bàn trống, đang dọn, ngừng dùng")
	@PostMapping("/tables/{id}/status")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void status(@PathVariable long id, @RequestBody Map<String, String> body) {
		floors.setStatus(id, body.get("status"));
	}

}
