package com.foodeats.config;

import com.foodeats.model.*;
import com.foodeats.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final MerchantRepository merchantRepository;
    private final CategoryRepository categoryRepository;
    private final MenuItemRepository menuItemRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, MerchantRepository merchantRepository,
                           CategoryRepository categoryRepository, MenuItemRepository menuItemRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.merchantRepository = merchantRepository;
        this.categoryRepository = categoryRepository;
        this.menuItemRepository = menuItemRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            String encodedPassword = passwordEncoder.encode("password123");

            // Create Users
            User customer = userRepository.save(new User("Sokha Customer", "customer@example.com", encodedPassword, "+85512345678", UserRole.CUSTOMER));
            User merchantOwner = userRepository.save(new User("Bopha Merchant", "merchant@example.com", encodedPassword, "+85598765432", UserRole.MERCHANT));
            User driver = userRepository.save(new User("Dara Driver", "driver@example.com", encodedPassword, "+85588776655", UserRole.DRIVER));
            User admin = userRepository.save(new User("System Admin", "admin@example.com", encodedPassword, "+85511223344", UserRole.ADMIN));

            // Create Merchants
            Merchant m1 = merchantRepository.save(new Merchant(
                    merchantOwner,
                    "Zando Burger & Grill",
                    "Juicy gourmet burgers, crispy fries, and refreshing cold drinks.",
                    "https://images.unsplash.com/photo-1568901346375-23c9450c58cd",
                    4.8, 1.50, 20, true, "Street 271, Phnom Penh"
            ));

            Merchant m2 = merchantRepository.save(new Merchant(
                    merchantOwner,
                    "Sakura Sushi & Ramen",
                    "Authentic Japanese sushi rolls, ramen bowls, and sashimi.",
                    "https://images.unsplash.com/photo-1579871494447-9811cf80d66c",
                    4.7, 2.00, 30, true, "Monivong Blvd, Phnom Penh"
            ));

            Merchant m3 = merchantRepository.save(new Merchant(
                    merchantOwner,
                    "Khmer Coffee & Bakery",
                    "Fresh artisan coffee, milk tea, and freshly baked pastries.",
                    "https://images.unsplash.com/photo-1509042239860-f550ce710b93",
                    4.9, 1.00, 15, true, "Norodom Blvd, Phnom Penh"
            ));

            // Categories & Items for M1
            Category cat1 = categoryRepository.save(new Category(m1, "Popular Burgers"));
            Category cat2 = categoryRepository.save(new Category(m1, "Sides & Drinks"));

            menuItemRepository.save(new MenuItem(cat1, "Double Cheese Delight", "Two beef patties, sharp cheddar, special Zando sauce", 6.50, "https://images.unsplash.com/photo-1568901346375-23c9450c58cd", true));
            menuItemRepository.save(new MenuItem(cat1, "Crispy Chicken Burger", "Crispy fried chicken breast, coleslaw, pickles", 5.00, "https://images.unsplash.com/photo-1625813506062-0aeb1d7a094b", true));
            menuItemRepository.save(new MenuItem(cat2, "Loaded French Fries", "Golden fries topped with cheese sauce and bacon bits", 2.50, "https://images.unsplash.com/photo-1573080496219-bb080dd4f877", true));

            // Categories & Items for M2
            Category cat3 = categoryRepository.save(new Category(m2, "Special Rolls"));
            Category cat4 = categoryRepository.save(new Category(m2, "Hot Ramen"));

            menuItemRepository.save(new MenuItem(cat3, "Salmon Aburi Roll", "Torched salmon with spicy mayo and unagi sauce", 8.00, "https://images.unsplash.com/photo-1579871494447-9811cf80d66c", true));
            menuItemRepository.save(new MenuItem(cat4, "Tonkotsu Pork Ramen", "Rich pork bone broth, chashu, soft boiled egg", 7.50, "https://images.unsplash.com/photo-1569718212165-3a8278d5f624", true));

            // Categories & Items for M3
            Category cat5 = categoryRepository.save(new Category(m3, "Artisan Coffee"));
            menuItemRepository.save(new MenuItem(cat5, "Iced Latte", "Double shot espresso with fresh milk", 3.00, "https://images.unsplash.com/photo-1517701604599-bb29b565090c", true));
        }
    }
}
