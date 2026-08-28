package com.foodeats.controller;

import com.foodeats.model.Category;
import com.foodeats.model.Merchant;
import com.foodeats.repository.CategoryRepository;
import com.foodeats.repository.MerchantRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryRepository categoryRepository;
    private final MerchantRepository merchantRepository;

    public CategoryController(CategoryRepository categoryRepository, MerchantRepository merchantRepository) {
        this.categoryRepository = categoryRepository;
        this.merchantRepository = merchantRepository;
    }

    @GetMapping
    public List<Category> getCategories(@RequestParam(required = false) Long merchantId) {
        if (merchantId != null) {
            return categoryRepository.findByMerchantIdOrderByDisplayOrderAsc(merchantId);
        }
        return categoryRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable Long id) {
        return categoryRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createCategory(@RequestBody Map<String, Object> payload) {
        String name = (String) payload.get("name");
        Long merchantId = payload.get("merchantId") != null ? Long.valueOf(payload.get("merchantId").toString()) : 1L;
        Integer displayOrder = payload.get("displayOrder") != null ? Integer.valueOf(payload.get("displayOrder").toString()) : 0;

        Optional<Merchant> merchantOpt = merchantRepository.findById(merchantId);
        if (merchantOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Merchant not found with id: " + merchantId));
        }

        Category category = new Category(merchantOpt.get(), name, displayOrder);
        Category saved = categoryRepository.save(category);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCategory(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        return categoryRepository.findById(id).map(existing -> {
            if (payload.containsKey("name")) {
                existing.setName((String) payload.get("name"));
            }
            if (payload.containsKey("displayOrder")) {
                existing.setDisplayOrder(Integer.valueOf(payload.get("displayOrder").toString()));
            }
            return ResponseEntity.ok(categoryRepository.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        if (categoryRepository.existsById(id)) {
            categoryRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
