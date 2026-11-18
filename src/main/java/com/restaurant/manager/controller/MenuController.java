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

import com.restaurant.manager.dto.request.CategoryRequest;
import com.restaurant.manager.dto.request.ItemRequest;
import com.restaurant.manager.dto.request.OnCreate;
import com.restaurant.manager.dto.request.TagRequest;
import com.restaurant.manager.dto.response.CategoryResponse;
import com.restaurant.manager.dto.response.ItemResponse;
import com.restaurant.manager.dto.response.TagResponse;
import com.restaurant.manager.service.MenuService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Thực đơn")
@RestController
@RequestMapping("/api/v1")
public class MenuController {

	private final MenuService menu;

	public MenuController(MenuService menu) {
		this.menu = menu;
	}

	@GetMapping("/categories")
	public List<CategoryResponse> categories() {
		return menu.categories();
	}

	@PostMapping("/categories")
	@ResponseStatus(HttpStatus.CREATED)
	public CategoryResponse createCategory(@Validated(OnCreate.class) @RequestBody CategoryRequest req) {
		return menu.createCategory(req);
	}

	@PatchMapping("/categories/{id}")
	public CategoryResponse updateCategory(@PathVariable long id, @Valid @RequestBody CategoryRequest req) {
		return menu.updateCategory(id, req);
	}

	@DeleteMapping("/categories/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteCategory(@PathVariable long id) {
		menu.deleteCategory(id);
	}

	@GetMapping("/tags")
	public List<TagResponse> tags() {
		return menu.tags();
	}

	@PostMapping("/tags")
	@ResponseStatus(HttpStatus.CREATED)
	public TagResponse createTag(@Validated(OnCreate.class) @RequestBody TagRequest req) {
		return menu.createTag(req);
	}

	@PatchMapping("/tags/{id}")
	public TagResponse updateTag(@PathVariable long id, @Valid @RequestBody TagRequest req) {
		return menu.updateTag(id, req);
	}

	@DeleteMapping("/tags/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteTag(@PathVariable long id) {
		menu.deleteTag(id);
	}

	@GetMapping("/items")
	public List<ItemResponse> items() {
		return menu.items();
	}

	@PostMapping("/items")
	@ResponseStatus(HttpStatus.CREATED)
	public ItemResponse createItem(@Validated(OnCreate.class) @RequestBody ItemRequest req) {
		return menu.createItem(req);
	}

	@PatchMapping("/items/{id}")
	public ItemResponse updateItem(@PathVariable long id, @Valid @RequestBody ItemRequest req) {
		return menu.updateItem(id, req);
	}

	@DeleteMapping("/items/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteItem(@PathVariable long id) {
		menu.deleteItem(id);
	}

}
