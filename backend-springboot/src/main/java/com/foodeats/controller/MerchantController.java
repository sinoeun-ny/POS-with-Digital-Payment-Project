package com.foodeats.controller;

import com.foodeats.model.Merchant;
import com.foodeats.repository.MerchantRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/merchants")
public class MerchantController {

    private final MerchantRepository merchantRepository;

    public MerchantController(MerchantRepository merchantRepository) {
        this.merchantRepository = merchantRepository;
    }

    @GetMapping
    public List<Merchant> getAllMerchants(@RequestParam(required = false) String search) {
        if (search != null && !search.trim().isEmpty()) {
            return merchantRepository.findByNameContainingIgnoreCase(search);
        }
        return merchantRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Merchant> getMerchantById(@PathVariable Long id) {
        return merchantRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Merchant createMerchant(@RequestBody Merchant merchant) {
        return merchantRepository.save(merchant);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Merchant> updateMerchant(@PathVariable Long id, @RequestBody Merchant updated) {
        return merchantRepository.findById(id).map(existing -> {
            existing.setName(updated.getName());
            existing.setDescription(updated.getDescription());
            existing.setImageUrl(updated.getImageUrl());
            existing.setDeliveryFee(updated.getDeliveryFee());
            existing.setDeliveryTimeMins(updated.getDeliveryTimeMins());
            existing.setIsOpen(updated.getIsOpen());
            existing.setAddress(updated.getAddress());
            return ResponseEntity.ok(merchantRepository.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMerchant(@PathVariable Long id) {
        if (merchantRepository.existsById(id)) {
            merchantRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
