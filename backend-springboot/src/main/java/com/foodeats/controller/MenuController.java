package com.foodeats.controller;

import com.foodeats.model.Category;
import com.foodeats.model.ItemOption;
import com.foodeats.model.MenuItem;
import com.foodeats.repository.CategoryRepository;
import com.foodeats.repository.ItemOptionRepository;
import com.foodeats.repository.MenuItemRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/menu")
public class MenuController {

    private final MenuItemRepository menuItemRepository;
    private final CategoryRepository categoryRepository;
    private final ItemOptionRepository itemOptionRepository;

    public MenuController(MenuItemRepository menuItemRepository,
                          CategoryRepository categoryRepository,
                          ItemOptionRepository itemOptionRepository) {
        this.menuItemRepository = menuItemRepository;
        this.categoryRepository = categoryRepository;
        this.itemOptionRepository = itemOptionRepository;
    }

    @GetMapping
    public List<MenuItem> getMenuItems(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long merchantId,
            @RequestParam(required = false) String search) {
        if (categoryId != null) {
            return menuItemRepository.findByCategoryId(categoryId);
        }
        if (merchantId != null) {
            return menuItemRepository.findByMerchantId(merchantId);
        }
        if (search != null && !search.trim().isEmpty()) {
            return menuItemRepository.findByNameContainingIgnoreCase(search);
        }
        return menuItemRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<MenuItem> getMenuItemById(@PathVariable Long id) {
        return menuItemRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createMenuItem(@RequestBody Map<String, Object> payload) {
        String name = (String) payload.get("name");
        String description = (String) payload.get("description");
        Double price = Double.valueOf(payload.get("price").toString());
        String imageUrl = (String) payload.get("imageUrl");
        Boolean isAvailable = payload.get("isAvailable") != null ? Boolean.valueOf(payload.get("isAvailable").toString()) : true;
        Integer prepTime = payload.get("prepTimeMinutes") != null ? Integer.valueOf(payload.get("prepTimeMinutes").toString()) : 15;
        String dietaryTag = (String) payload.get("dietaryTag");

        Long categoryId = payload.get("categoryId") != null ? Long.valueOf(payload.get("categoryId").toString()) : 1L;
        Optional<Category> categoryOpt = categoryRepository.findById(categoryId);
        if (categoryOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Category not found with id: " + categoryId));
        }

        MenuItem menuItem = new MenuItem(categoryOpt.get(), name, description, price, imageUrl, isAvailable);
        menuItem.setPrepTimeMinutes(prepTime);
        menuItem.setDietaryTag(dietaryTag);
        MenuItem saved = menuItemRepository.save(menuItem);

        // Process options if provided
        if (payload.containsKey("options") && payload.get("options") instanceof List) {
            List<Map<String, Object>> optionsList = (List<Map<String, Object>>) payload.get("options");
            for (Map<String, Object> optMap : optionsList) {
                String group = optMap.get("optionGroup") != null ? optMap.get("optionGroup").toString() : "Options";
                String optName = optMap.get("optionName") != null ? optMap.get("optionName").toString() : "";
                Double extraPrice = optMap.get("priceAdjustment") != null ? Double.valueOf(optMap.get("priceAdjustment").toString()) : 0.0;
                Boolean optAvailable = optMap.get("isAvailable") != null ? Boolean.valueOf(optMap.get("isAvailable").toString()) : true;
                if (!optName.trim().isEmpty()) {
                    ItemOption itemOption = new ItemOption(saved, group, optName, extraPrice, optAvailable);
                    itemOptionRepository.save(itemOption);
                }
            }
        }

        return ResponseEntity.ok(menuItemRepository.findById(saved.getId()).orElse(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateMenuItem(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        return menuItemRepository.findById(id).map(existing -> {
            if (payload.containsKey("name")) existing.setName((String) payload.get("name"));
            if (payload.containsKey("description")) existing.setDescription((String) payload.get("description"));
            if (payload.containsKey("price")) existing.setPrice(Double.valueOf(payload.get("price").toString()));
            if (payload.containsKey("imageUrl")) existing.setImageUrl((String) payload.get("imageUrl"));
            if (payload.containsKey("isAvailable")) existing.setIsAvailable(Boolean.valueOf(payload.get("isAvailable").toString()));
            if (payload.containsKey("prepTimeMinutes")) existing.setPrepTimeMinutes(Integer.valueOf(payload.get("prepTimeMinutes").toString()));
            if (payload.containsKey("dietaryTag")) existing.setDietaryTag((String) payload.get("dietaryTag"));

            if (payload.containsKey("categoryId")) {
                Long categoryId = Long.valueOf(payload.get("categoryId").toString());
                categoryRepository.findById(categoryId).ifPresent(existing::setCategory);
            }

            MenuItem saved = menuItemRepository.save(existing);
            return ResponseEntity.ok(saved);
        }).orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/availability")
    public ResponseEntity<?> toggleAvailability(@PathVariable Long id, @RequestParam(required = false) Boolean isAvailable) {
        return menuItemRepository.findById(id).map(existing -> {
            boolean nextState = isAvailable != null ? isAvailable : !Boolean.TRUE.equals(existing.getIsAvailable());
            existing.setIsAvailable(nextState);
            MenuItem saved = menuItemRepository.save(existing);
            return ResponseEntity.ok(Map.of(
                    "id", saved.getId(),
                    "name", saved.getName(),
                    "isAvailable", saved.getIsAvailable(),
                    "message", "Item inventory updated to " + (nextState ? "IN_STOCK" : "OUT_OF_STOCK")
            ));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMenuItem(@PathVariable Long id) {
        if (menuItemRepository.existsById(id)) {
            menuItemRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    // ==========================================
    // ITEM OPTIONS (US-008: Customization Engine)
    // ==========================================

    @GetMapping("/{id}/options")
    public ResponseEntity<List<ItemOption>> getItemOptions(@PathVariable Long id) {
        return ResponseEntity.ok(itemOptionRepository.findByMenuItemId(id));
    }

    @PostMapping("/{id}/options")
    public ResponseEntity<?> addItemOption(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        Optional<MenuItem> itemOpt = menuItemRepository.findById(id);
        if (itemOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        String group = payload.get("optionGroup") != null ? payload.get("optionGroup").toString() : "Options";
        String optionName = payload.get("optionName") != null ? payload.get("optionName").toString() : "";
        Double extraPrice = payload.get("priceAdjustment") != null ? Double.valueOf(payload.get("priceAdjustment").toString()) : 0.0;
        Boolean isAvailable = payload.get("isAvailable") != null ? Boolean.valueOf(payload.get("isAvailable").toString()) : true;

        if (optionName.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Option name is required"));
        }

        ItemOption itemOption = new ItemOption(itemOpt.get(), group, optionName, extraPrice, isAvailable);
        ItemOption saved = itemOptionRepository.save(itemOption);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/options/{optionId}")
    public ResponseEntity<?> updateItemOption(@PathVariable Long optionId, @RequestBody Map<String, Object> payload) {
        return itemOptionRepository.findById(optionId).map(existing -> {
            if (payload.containsKey("optionGroup")) existing.setOptionGroup(payload.get("optionGroup").toString());
            if (payload.containsKey("optionName")) existing.setOptionName(payload.get("optionName").toString());
            if (payload.containsKey("priceAdjustment")) existing.setPriceAdjustment(Double.valueOf(payload.get("priceAdjustment").toString()));
            if (payload.containsKey("isAvailable")) existing.setIsAvailable(Boolean.valueOf(payload.get("isAvailable").toString()));
            return ResponseEntity.ok(itemOptionRepository.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/options/{optionId}")
    public ResponseEntity<Void> deleteItemOption(@PathVariable Long optionId) {
        if (itemOptionRepository.existsById(optionId)) {
            itemOptionRepository.deleteById(optionId);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
