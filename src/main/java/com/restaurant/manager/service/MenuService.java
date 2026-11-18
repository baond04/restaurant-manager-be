package com.restaurant.manager.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restaurant.manager.dto.request.CategoryRequest;
import com.restaurant.manager.dto.request.ItemRequest;
import com.restaurant.manager.dto.request.TagRequest;
import com.restaurant.manager.dto.response.CategoryResponse;
import com.restaurant.manager.dto.response.ItemResponse;
import com.restaurant.manager.dto.response.TagResponse;
import com.restaurant.manager.entity.Category;
import com.restaurant.manager.entity.Item;
import com.restaurant.manager.entity.Tag;
import com.restaurant.manager.exception.ApiException;
import com.restaurant.manager.repository.CategoryRepository;
import com.restaurant.manager.repository.ItemRepository;
import com.restaurant.manager.repository.TagRepository;
import com.restaurant.manager.security.CurrentUser;

/** Thực đơn: danh mục, thẻ, món. Ai cũng xem được; công thức (giá vốn) chỉ nhân viên xem; sửa cần menu:write. */
@Service
public class MenuService {

	private static final String WRITE = "menu:write";

	private final CategoryRepository categories;
	private final TagRepository tags;
	private final ItemRepository items;

	public MenuService(CategoryRepository categories, TagRepository tags, ItemRepository items) {
		this.categories = categories;
		this.tags = tags;
		this.items = items;
	}

	@Transactional(readOnly = true)
	public List<CategoryResponse> categories() {
		return categories.findAll().stream().map(CategoryResponse::of).toList();
	}

	@Transactional
	public CategoryResponse createCategory(CategoryRequest req) {
		CurrentUser.get().require(WRITE);
		var e = new Category();
		req.applyTo(e);
		return CategoryResponse.of(categories.save(e));
	}

	@Transactional
	public CategoryResponse updateCategory(long id, CategoryRequest req) {
		CurrentUser.get().require(WRITE);
		Category e = Repos.find(categories, id);
		req.applyTo(e);
		return CategoryResponse.of(e);
	}

	@Transactional
	public void deleteCategory(long id) {
		CurrentUser.get().require(WRITE);
		if (items.existsByCategoryIdAndDeletedAtIsNull(id)) {
			throw ApiException.conflict("Danh mục còn món. Chuyển món sang danh mục khác trước.");
		}
		categories.delete(Repos.find(categories, id));
	}

	@Transactional(readOnly = true)
	public List<TagResponse> tags() {
		return tags.findAll().stream().map(TagResponse::of).toList();
	}

	@Transactional
	public TagResponse createTag(TagRequest req) {
		CurrentUser.get().require(WRITE);
		var e = new Tag();
		req.applyTo(e);
		return TagResponse.of(tags.save(e));
	}

	@Transactional
	public TagResponse updateTag(long id, TagRequest req) {
		CurrentUser.get().require(WRITE);
		Tag e = Repos.find(tags, id);
		req.applyTo(e);
		return TagResponse.of(e);
	}

	@Transactional
	public void deleteTag(long id) {
		CurrentUser.get().require(WRITE);
		Tag t = Repos.find(tags, id);
		items.findAll().forEach(i -> i.getTagIds().remove(id)); // gỡ thẻ khỏi các món trước khi xóa
		tags.delete(t);
	}

	@Transactional(readOnly = true)
	public List<ItemResponse> items() {
		boolean staff = CurrentUser.get().canAny(java.util.Set.of("menu:view", WRITE, "inventory:view"));
		return items.findByDeletedAtIsNullOrderById().stream().map(i -> staff ? ItemResponse.of(i) : withoutRecipe(i)).toList();
	}

	@Transactional
	public ItemResponse createItem(ItemRequest req) {
		CurrentUser.get().require(WRITE);
		Repos.find(categories, req.categoryId());
		var e = new Item();
		req.applyTo(e);
		return ItemResponse.of(items.save(e));
	}

	@Transactional
	public ItemResponse updateItem(long id, ItemRequest req) {
		CurrentUser.get().require(WRITE);
		Item e = Repos.find(items, id);
		if (e.getDeletedAt() != null) {
			throw ApiException.notFound("Món không còn trong thực đơn");
		}
		req.applyTo(e);
		return ItemResponse.of(e);
	}

	/** Xóa mềm: hóa đơn cũ vẫn tham chiếu được món. */
	@Transactional
	public void deleteItem(long id) {
		CurrentUser.get().require(WRITE);
		Repos.find(items, id).setDeletedAt(Instant.now());
	}

	private static ItemResponse withoutRecipe(Item i) {
		var r = ItemResponse.of(i);
		return new ItemResponse(r.id(), r.categoryId(), r.name(), r.description(), r.price(), r.image(), r.isAvailable(), r.spicyLevel(),
				r.prepMinutes(), r.deletedAt(), r.tagIds(), new ArrayList<>(), r.createdAt(), r.createdBy(), r.updatedAt(), r.updatedBy());
	}

}
