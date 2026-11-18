package com.restaurant.manager.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restaurant.manager.dto.request.IngredientRequest;
import com.restaurant.manager.dto.response.IngredientResponse;
import com.restaurant.manager.dto.response.InventoryTransactionResponse;
import com.restaurant.manager.entity.Ingredient;
import com.restaurant.manager.entity.InventoryTransaction;
import com.restaurant.manager.entity.Item;
import com.restaurant.manager.entity.OrderItem;
import com.restaurant.manager.exception.ApiException;
import com.restaurant.manager.repository.IngredientRepository;
import com.restaurant.manager.repository.InventoryTransactionRepository;
import com.restaurant.manager.repository.ItemRepository;
import com.restaurant.manager.security.CurrentUser;

/** Nguyên liệu và phiếu kho. Tồn kho chỉ đổi qua phiếu (nhập, xuất, điều chỉnh) hoặc khi gọi món (trừ theo công thức). */
@Service
public class InventoryService {

	private final IngredientRepository ingredients;
	private final InventoryTransactionRepository txs;
	private final ItemRepository items;

	public InventoryService(IngredientRepository ingredients, InventoryTransactionRepository txs, ItemRepository items) {
		this.ingredients = ingredients;
		this.txs = txs;
		this.items = items;
	}

	@Transactional(readOnly = true)
	public List<IngredientResponse> ingredients() {
		CurrentUser.get().requireAny(Set.of("inventory:view", "menu:write"));
		return ingredients.findAll().stream().map(IngredientResponse::of).toList();
	}

	@Transactional
	public IngredientResponse create(IngredientRequest req) {
		CurrentUser.get().require("inventory:write");
		var e = new Ingredient();
		req.applyTo(e);
		return IngredientResponse.of(ingredients.save(e));
	}

	@Transactional
	public IngredientResponse update(long id, IngredientRequest req) {
		CurrentUser.get().require("inventory:write");
		Ingredient e = Repos.find(ingredients, id);
		req.applyTo(e);
		return IngredientResponse.of(e);
	}

	@Transactional
	public void delete(long id) {
		CurrentUser.get().require("inventory:write");
		if (items.usesIngredient(id)) {
			throw ApiException.conflict("Nguyên liệu đang dùng trong công thức món.");
		}
		ingredients.delete(Repos.find(ingredients, id));
	}

	@Transactional(readOnly = true)
	public List<InventoryTransactionResponse> transactions() {
		CurrentUser.get().require("inventory:view");
		return txs.findAll().stream().map(InventoryTransactionResponse::of).toList();
	}

	public record TxRequest(Long ingredientId, String type, BigDecimal qty, Long unitCost, String note) {
	}

	/** Phiếu kho thủ công; xuất quá tồn thì từ chối. */
	@Transactional
	public InventoryTransactionResponse addTransaction(TxRequest req) {
		CurrentUser p = CurrentUser.get();
		p.require("inventory:write");
		ApiException.requireOneOf(req.type(), "Loại phiếu không hợp lệ", "IMPORT", "EXPORT", "ADJUST");
		if (req.qty() == null || req.qty().signum() == 0) {
			throw ApiException.badRequest("Số lượng phải khác 0");
		}
		Ingredient ing = ingredients.lockById(req.ingredientId()).orElseThrow(() -> ApiException.notFound("Không tìm thấy nguyên liệu"));
		BigDecimal delta = "EXPORT".equals(req.type()) ? req.qty().abs().negate() : req.qty();
		if (ing.getStock().add(delta).signum() < 0) {
			throw ApiException.conflict("Tồn kho " + ing.getName() + " không đủ");
		}
		ing.setStock(ing.getStock().add(delta));
		var tx = new InventoryTransaction();
		tx.setIngredientId(ing.getId());
		tx.setType(req.type());
		tx.setQty(req.qty());
		tx.setUnitCost(req.unitCost() == null ? ing.getCost() : req.unitCost());
		tx.setNote(req.note());
		tx.setEmployeeId(p.userId());
		return InventoryTransactionResponse.of(txs.save(tx));
	}

	/** Trừ kho theo công thức khi gọi món; thiếu thì kho về 0 và vẫn ghi phiếu để quản lý thấy chênh lệch. */
	@Transactional
	public void consume(Item item, int qty, OrderItem line) {
		for (var r : item.getRecipe()) {
			Ingredient ing = ingredients.lockById(r.getIngredientId()).orElseThrow();
			BigDecimal used = r.getQty().multiply(BigDecimal.valueOf(qty));
			ing.setStock(ing.getStock().subtract(used).max(BigDecimal.ZERO));
			var tx = new InventoryTransaction();
			tx.setIngredientId(ing.getId());
			tx.setType("CONSUME");
			tx.setQty(used);
			tx.setUnitCost(ing.getCost());
			tx.setNote("Món #" + line.getId());
			txs.save(tx);
		}
	}

}
