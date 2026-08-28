package com.foodeats.controller;

import com.foodeats.model.Merchant;
import com.foodeats.model.User;
import com.foodeats.repository.MerchantRepository;
import com.foodeats.repository.UserRepository;
import com.foodeats.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/merchants")
public class MerchantController {

    private final MerchantRepository merchantRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public MerchantController(MerchantRepository merchantRepository, UserRepository userRepository, JwtUtil jwtUtil) {
        this.merchantRepository = merchantRepository;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
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

    @GetMapping("/my-store")
    public ResponseEntity<?> getMyStore(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtUtil.extractEmail(token);
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isPresent()) {
                Optional<Merchant> merchantOpt = merchantRepository.findByOwnerId(userOpt.get().getId());
                if (merchantOpt.isPresent()) {
                    return ResponseEntity.ok(merchantOpt.get());
                }
            }
        }
        // Fallback to first merchant if not authenticated or demoing
        List<Merchant> merchants = merchantRepository.findAll();
        if (!merchants.isEmpty()) {
            return ResponseEntity.ok(merchants.get(0));
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<?> createMerchant(@RequestBody Merchant merchant, @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtUtil.extractEmail(token);
            userRepository.findByEmail(email).ifPresent(merchant::setOwner);
        }
        Merchant saved = merchantRepository.save(merchant);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Merchant> updateMerchant(@PathVariable Long id, @RequestBody Merchant updated) {
        return merchantRepository.findById(id).map(existing -> {
            if (updated.getName() != null) existing.setName(updated.getName());
            if (updated.getDescription() != null) existing.setDescription(updated.getDescription());
            if (updated.getImageUrl() != null) existing.setImageUrl(updated.getImageUrl());
            if (updated.getBannerUrl() != null) existing.setBannerUrl(updated.getBannerUrl());
            if (updated.getPhone() != null) existing.setPhone(updated.getPhone());
            if (updated.getCuisineType() != null) existing.setCuisineType(updated.getCuisineType());
            if (updated.getOpeningHours() != null) existing.setOpeningHours(updated.getOpeningHours());
            if (updated.getDeliveryFee() != null) existing.setDeliveryFee(updated.getDeliveryFee());
            if (updated.getDeliveryTimeMins() != null) existing.setDeliveryTimeMins(updated.getDeliveryTimeMins());
            if (updated.getIsOpen() != null) existing.setIsOpen(updated.getIsOpen());
            if (updated.getAddress() != null) existing.setAddress(updated.getAddress());
            if (updated.getCity() != null) existing.setCity(updated.getCity());
            return ResponseEntity.ok(merchantRepository.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> toggleStoreStatus(@PathVariable Long id, @RequestParam(required = false) Boolean isOpen) {
        return merchantRepository.findById(id).map(existing -> {
            boolean nextStatus = isOpen != null ? isOpen : !Boolean.TRUE.equals(existing.getIsOpen());
            existing.setIsOpen(nextStatus);
            Merchant saved = merchantRepository.save(existing);
            return ResponseEntity.ok(Map.of("id", saved.getId(), "isOpen", saved.getIsOpen(), "message", "Store status updated to " + (nextStatus ? "OPEN" : "CLOSED")));
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
