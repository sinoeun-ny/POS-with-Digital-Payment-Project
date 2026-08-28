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
    private final ItemOptionRepository itemOptionRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, MerchantRepository merchantRepository,
                           CategoryRepository categoryRepository, MenuItemRepository menuItemRepository,
                           ItemOptionRepository itemOptionRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.merchantRepository = merchantRepository;
        this.categoryRepository = categoryRepository;
        this.menuItemRepository = menuItemRepository;
        this.itemOptionRepository = itemOptionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            String encodedPassword = passwordEncoder.encode("password123");

            // 1. Create Users for all roles
            User customer = userRepository.save(new User("Sokha Customer", "customer@example.com", encodedPassword, "+85512345678", UserRole.CUSTOMER));
            User merchantOwner = userRepository.save(new User("Bopha Merchant", "merchant@example.com", encodedPassword, "+85598765432", UserRole.MERCHANT));
            User driver = userRepository.save(new User("Dara Driver", "driver@example.com", encodedPassword, "+85588776655", UserRole.DRIVER));
            User admin = userRepository.save(new User("System Admin", "admin@example.com", encodedPassword, "+85511223344", UserRole.ADMIN));

            // 2. Create Merchants with full profile setup (US-005)
            Merchant m1 = new Merchant(
                    merchantOwner,
                    "Zando Burger & Grill",
                    "Juicy artisan smash burgers, crispy truffle fries, and premium thick milkshakes crafted daily.",
                    "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=600&auto=format&fit=crop&q=80",
                    4.8, 1.50, 20, true, "Street 271, Sangkat Phsar Doeum Thkov, Phnom Penh"
            );
            m1.setBannerUrl("https://images.unsplash.com/photo-1550547660-d9450f859349?w=1200&auto=format&fit=crop&q=80");
            m1.setPhone("+855 23 888 999");
            m1.setCuisineType("Gourmet American");
            m1.setOpeningHours("10:00 AM - 10:30 PM");
            m1 = merchantRepository.save(m1);

            Merchant m2 = new Merchant(
                    merchantOwner,
                    "Sakura Sushi & Ramen Bar",
                    "Authentic Japanese tonkotsu ramen bowls, torched salmon aburi rolls, and fresh seasonal sashimi.",
                    "https://images.unsplash.com/photo-1579871494447-9811cf80d66c?w=600&auto=format&fit=crop&q=80",
                    4.9, 2.00, 30, true, "Monivong Blvd, Boeung Keng Kang 1, Phnom Penh"
            );
            m2.setBannerUrl("https://images.unsplash.com/photo-1617196034796-73dfa7b1fd56?w=1200&auto=format&fit=crop&q=80");
            m2.setPhone("+855 23 777 666");
            m2.setCuisineType("Japanese Artisan");
            m2.setOpeningHours("11:00 AM - 11:00 PM");
            m2 = merchantRepository.save(m2);

            Merchant m3 = new Merchant(
                    merchantOwner,
                    "Khmer Coffee & Patisserie",
                    "Traditional slow-drip Mondulkiri iced coffee, French butter croissants, and fresh coconut pandan waffles.",
                    "https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=600&auto=format&fit=crop&q=80",
                    4.9, 1.00, 15, true, "Norodom Blvd, Daun Penh, Phnom Penh"
            );
            m3.setBannerUrl("https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?w=1200&auto=format&fit=crop&q=80");
            m3.setPhone("+855 23 555 444");
            m3.setCuisineType("Café & Bakery");
            m3.setOpeningHours("07:00 AM - 09:00 PM");
            m3 = merchantRepository.save(m3);

            // 3. Create Categories (US-006)
            Category cat1 = categoryRepository.save(new Category(m1, "Signature Burgers", 1));
            Category cat2 = categoryRepository.save(new Category(m1, "Artisan Sides", 2));
            Category cat3 = categoryRepository.save(new Category(m1, "Craft Beverages", 3));

            Category cat4 = categoryRepository.save(new Category(m2, "Specialty Rolls", 1));
            Category cat5 = categoryRepository.save(new Category(m2, "Hot Broth Ramen", 2));

            Category cat6 = categoryRepository.save(new Category(m3, "Single Origin Coffee", 1));
            Category cat7 = categoryRepository.save(new Category(m3, "Fresh Bakery", 2));

            // 4. Create Menu Items with US-007 (Image URLs, Availability, Prep Time, Tags)
            MenuItem item1 = new MenuItem(cat1, "Double Truffle Smash Burger", "Double black angus beef patties, aged white cheddar, sautéed portobello, and black truffle aioli on a toasted brioche bun.", 7.25, "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=600&auto=format&fit=crop&q=80", true);
            item1.setPrepTimeMinutes(15);
            item1.setDietaryTag("Chef Special");
            item1.setPopularScore(99);
            item1 = menuItemRepository.save(item1);

            // 5. Create Item Customization Options (US-008)
            itemOptionRepository.save(new ItemOption(item1, "Patty Size", "Double 150g (Standard)", 0.00, true));
            itemOptionRepository.save(new ItemOption(item1, "Patty Size", "Triple Monster 225g", 2.25, true));
            itemOptionRepository.save(new ItemOption(item1, "Add-Ons", "Extra Aged Cheddar Slice", 0.75, true));
            itemOptionRepository.save(new ItemOption(item1, "Add-Ons", "Smoked Applewood Bacon", 1.25, true));
            itemOptionRepository.save(new ItemOption(item1, "Add-Ons", "Fried Cage-Free Egg", 0.60, true));

            MenuItem item2 = new MenuItem(cat1, "Spicy Nashville Crispy Chicken", "Buttermilk-brined crispy chicken thigh coated with Nashville cayenne glaze, spicy pickled cucumbers, and house slaw.", 5.75, "https://images.unsplash.com/photo-1625813506062-0aeb1d7a094b?w=600&auto=format&fit=crop&q=80", true);
            item2.setPrepTimeMinutes(12);
            item2.setDietaryTag("Spicy");
            item2.setPopularScore(94);
            item2 = menuItemRepository.save(item2);

            itemOptionRepository.save(new ItemOption(item2, "Spice Heat Level", "Mild Heat", 0.00, true));
            itemOptionRepository.save(new ItemOption(item2, "Spice Heat Level", "Hot & Crispy", 0.00, true));
            itemOptionRepository.save(new ItemOption(item2, "Spice Heat Level", "Extra Blazing Ghost Pepper", 0.50, true));

            MenuItem item3 = new MenuItem(cat2, "Parmesan Truffle French Fries", "Hand-cut Idaho potato fries tossed with white truffle oil, freshly grated Grana Padano parmesan, and chives.", 2.95, "https://images.unsplash.com/photo-1573080496219-bb080dd4f877?w=600&auto=format&fit=crop&q=80", true);
            item3.setPrepTimeMinutes(8);
            item3.setDietaryTag("Vegetarian");
            item3 = menuItemRepository.save(item3);

            MenuItem item4 = new MenuItem(cat3, "Salted Caramel Shake", "Hand-spun Madagascar vanilla gelato with sea salt caramel ribbon and whipped cream.", 3.50, "https://images.unsplash.com/photo-1572490122747-3968b75cc699?w=600&auto=format&fit=crop&q=80", true);
            item4 = menuItemRepository.save(item4);

            // Items for Sakura
            MenuItem item5 = new MenuItem(cat4, "Torched Salmon Aburi Roll", "8 pieces. Fresh Norwegian salmon lightly torched with house spicy mayo, sweet unagi reduction, and tobiko.", 8.50, "https://images.unsplash.com/photo-1579871494447-9811cf80d66c?w=600&auto=format&fit=crop&q=80", true);
            item5.setPrepTimeMinutes(18);
            item5.setDietaryTag("Chef Special");
            item5 = menuItemRepository.save(item5);
            itemOptionRepository.save(new ItemOption(item5, "Portion Size", "8 Pieces", 0.00, true));
            itemOptionRepository.save(new ItemOption(item5, "Portion Size", "12 Pieces Party Size", 3.80, true));
            itemOptionRepository.save(new ItemOption(item5, "Preparation", "Regular Wasabi", 0.00, true));
            itemOptionRepository.save(new ItemOption(item5, "Preparation", "Extra Pickled Ginger & Wasabi", 0.50, true));

            MenuItem item6 = new MenuItem(cat5, "Signature Tonkotsu Black Ramen", "16-hour simmered pork bone broth, handcrafted springy noodles, slow-braised chashu pork, ajitsuke tamago egg, and black garlic oil.", 8.00, "https://images.unsplash.com/photo-1569718212165-3a8278d5f624?w=600&auto=format&fit=crop&q=80", true);
            item6.setPrepTimeMinutes(15);
            item6 = menuItemRepository.save(item6);
            itemOptionRepository.save(new ItemOption(item6, "Noodle Firmness", "Soft", 0.00, true));
            itemOptionRepository.save(new ItemOption(item6, "Noodle Firmness", "Standard Medium", 0.00, true));
            itemOptionRepository.save(new ItemOption(item6, "Noodle Firmness", "Hard / Firm (Katame)", 0.00, true));
            itemOptionRepository.save(new ItemOption(item6, "Extra Toppings", "Extra Braised Chashu (2 slices)", 2.00, true));
            itemOptionRepository.save(new ItemOption(item6, "Extra Toppings", "Extra Ajitsuke Ramen Egg", 1.00, true));

            // Items for Khmer Coffee
            MenuItem item7 = new MenuItem(cat6, "Mondulkiri Drip Iced Latte", "Double shot Mondulkiri mountain espresso over creamy condensed milk and crushed ice.", 2.75, "https://images.unsplash.com/photo-1517701604599-bb29b565090c?w=600&auto=format&fit=crop&q=80", true);
            item7 = menuItemRepository.save(item7);
            itemOptionRepository.save(new ItemOption(item7, "Sweetness Level", "100% Full Sweet", 0.00, true));
            itemOptionRepository.save(new ItemOption(item7, "Sweetness Level", "50% Less Sweet", 0.00, true));
            itemOptionRepository.save(new ItemOption(item7, "Sweetness Level", "0% Unsweetened / Black", 0.00, true));
            itemOptionRepository.save(new ItemOption(item7, "Milk Choice", "Fresh Milk", 0.00, true));
            itemOptionRepository.save(new ItemOption(item7, "Milk Choice", "Oat Milk (+ $0.60)", 0.60, true));

            MenuItem item8 = new MenuItem(cat7, "French Almond Butter Croissant", "Twice-baked flaky butter pastry filled with rich almond frangipane cream and sliced toasted almonds.", 2.50, "https://images.unsplash.com/photo-1555507036-ab1f4038808a?w=600&auto=format&fit=crop&q=80", true);
            item8.setDietaryTag("Vegetarian");
            item8 = menuItemRepository.save(item8);
        }
    }
}
