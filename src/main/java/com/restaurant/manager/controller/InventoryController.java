package com.restaurant.manager.controller;

import java.util.List;

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

import com.restaurant.manager.dto.request.IngredientRequest;
import com.restaurant.manager.dto.request.OnCreate;
import com.restaurant.manager.dto.response.IngredientResponse;
import com.restaurant.manager.dto.response.InventoryTransactionResponse;
import com.restaurant.manager.service.InventoryService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Kho nguyên liệu")
@RestController
@RequestMapping("/api/v1")
public class InventoryController {

	private final InventoryService inventory;

	public InventoryController(InventoryService inventory) {
		this.inventory = inventory;
	}

	@GetMapping("/ingredients")
	public List<IngredientResponse> ingredients() {
		return inventory.ingredients();
	}

	@PostMapping("/ingredients")
	@ResponseStatus(HttpStatus.CREATED)
	public IngredientResponse create(@Validated(OnCreate.class) @RequestBody IngredientRequest req) {
		return inventory.create(req);
	}

	@PatchMapping("/ingredients/{id}")
	public IngredientResponse update(@PathVariable long id, @Valid @RequestBody IngredientRequest req) {
		return inventory.update(id, req);
	}

	@DeleteMapping("/ingredients/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable long id) {
		inventory.delete(id);
	}

	@GetMapping("/inventory-transactions")
	public List<InventoryTransactionResponse> transactions() {
		return inventory.transactions();
	}

	@PostMapping("/inventory-transactions")
	@ResponseStatus(HttpStatus.CREATED)
	public InventoryTransactionResponse addTransaction(@RequestBody InventoryService.TxRequest req) {
		return inventory.addTransaction(req);
	}

}
