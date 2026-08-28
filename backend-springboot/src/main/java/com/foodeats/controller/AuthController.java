package com.foodeats.controller;

import com.foodeats.dto.*;
import com.foodeats.model.*;
import com.foodeats.repository.*;
import com.foodeats.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final MerchantRepository merchantRepository;
    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(UserRepository userRepository,
                          MerchantRepository merchantRepository,
                          CategoryRepository categoryRepository,
                          PasswordEncoder passwordEncoder,
                          JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.merchantRepository = merchantRepository;
        this.categoryRepository = categoryRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email is already registered. Please login instead."));
        }

        User user = new User(
                request.getName(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getPhone(),
                UserRole.CUSTOMER
        );

        User savedUser = userRepository.save(user);
        String token = jwtUtil.generateToken(savedUser.getEmail(), savedUser.getRole().name(), savedUser.getId());
        Long merchantId = null;

        // US-002: Auto-Onboarding for Merchant store
        if (savedUser.getRole() == UserRole.MERCHANT) {
            String storeName = (request.getRestaurantName() != null && !request.getRestaurantName().isBlank())
                    ? request.getRestaurantName()
                    : savedUser.getName() + " Kitchen";
            String cuisine = (request.getCuisineType() != null && !request.getCuisineType().isBlank())
                    ? request.getCuisineType()
                    : "Gourmet Specialties";
            String storeAddress = (request.getAddress() != null && !request.getAddress().isBlank())
                    ? request.getAddress()
                    : "Phnom Penh Central";

            Merchant merchant = new Merchant(
                    savedUser,
                    storeName,
                    "Welcome to " + storeName + "! Fresh gourmet meals made with love and delivered hot to your doorstep.",
                    "https://images.unsplash.com/photo-1555396273-367ea4eb4db5?w=600&auto=format&fit=crop&q=80",
                    4.9,
                    1.50,
                    20,
                    true,
                    storeAddress
            );
            merchant.setCuisineType(cuisine);
            merchant.setCity(request.getCity() != null ? request.getCity() : "Phnom Penh");
            merchant.setBannerUrl("https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=1200&auto=format&fit=crop&q=80");
            merchant.setPhone(savedUser.getPhone());
            Merchant savedMerchant = merchantRepository.save(merchant);
            merchantId = savedMerchant.getId();

            // Create initial category for this merchant
            Category defaultCategory = new Category(savedMerchant, "Signature Specials", 1);
            categoryRepository.save(defaultCategory);
        }

        return ResponseEntity.ok(new AuthResponse(token, savedUser.getId(), savedUser.getName(), savedUser.getEmail(), savedUser.getRole(), merchantId));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());

        if (userOpt.isEmpty() || !passwordEncoder.matches(request.getPassword(), userOpt.get().getPassword())) {
            return ResponseEntity.status(401).body(Map.of("message", "Invalid email or password"));
        }

        User user = userOpt.get();
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name(), user.getId());
        Long merchantId = null;

        if (user.getRole() == UserRole.MERCHANT) {
            Optional<Merchant> mOpt = merchantRepository.findByOwnerId(user.getId());
            if (mOpt.isPresent()) {
                merchantId = mOpt.get().getId();
            }
        }

        return ResponseEntity.ok(new AuthResponse(token, user.getId(), user.getName(), user.getEmail(), user.getRole(), merchantId));
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("message", "Missing or invalid Authorization header"));
        }
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOpt.get();
        return ResponseEntity.ok(Map.of(
                "id", user.getId(),
                "name", user.getName(),
                "email", user.getEmail(),
                "phone", user.getPhone() != null ? user.getPhone() : "",
                "role", user.getRole().name()
        ));
    }
}
