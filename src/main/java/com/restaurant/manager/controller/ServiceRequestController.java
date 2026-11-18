package com.restaurant.manager.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.restaurant.manager.dto.response.ServiceRequestResponse;
import com.restaurant.manager.service.ServiceRequestService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Yêu cầu của khách tại bàn")
@RestController
@RequestMapping("/api/v1")
public class ServiceRequestController {

	private final ServiceRequestService requests;

	public ServiceRequestController(ServiceRequestService requests) {
		this.requests = requests;
	}

	@GetMapping("/service-requests")
	public List<ServiceRequestResponse> list() {
		return requests.list();
	}

	@Operation(summary = "Khách gửi yêu cầu (gọi nhân viên, tính tiền, báo mở bàn...), xác định bàn bằng mã QR")
	@PostMapping("/table-sessions/request")
	@ResponseStatus(HttpStatus.CREATED)
	public ServiceRequestResponse create(@RequestBody Map<String, String> body) {
		return requests.create(body.get("qrToken"), body.get("type"));
	}

	@PostMapping("/service-requests/{id}/ack")
	public ServiceRequestResponse ack(@PathVariable long id) {
		return requests.handle(id, false);
	}

	@PostMapping("/service-requests/{id}/done")
	public ServiceRequestResponse done(@PathVariable long id) {
		return requests.handle(id, true);
	}

}
