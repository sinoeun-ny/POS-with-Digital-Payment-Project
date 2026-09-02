# Food Delivery System - Architecture & Implementation Guide

## 📋 Project Overview

This is a **Food Delivery Platform** (similar to UberEats/DoorDash) built with Spring Boot backend and Android (Kotlin) mobile app. The system supports multiple user roles: **Customer, Merchant, Driver, and Admin**.

---

## 🏗️ Current System Architecture

### **Backend Structure (Spring Boot)**
```
backend-springboot/
├── model/          # Entity classes (User, Order, Cart, Merchant, etc.)
├── repository/     # JPA Repositories for data access
├── controller/     # REST API endpoints
├── service/        # Business logic layer ✨ NEW
│   ├── CartService.java
│   ├── OrderService.java
│   └── impl/
│       ├── CartServiceImpl.java
│       └── OrderServiceImpl.java
├── dto/            # Data Transfer Objects
├── security/       # JWT Authentication & Security Config
└── config/         # Application configuration
```

### **Key Models**
- **User** - Supports 4 roles: CUSTOMER, MERCHANT, DRIVER, ADMIN
- **Merchant** - Restaurant/food provider with delivery fee, ratings, cuisine type
- **MenuItem** - Food items with prices, images, dietary tags, prep time
- **ItemOption** - Customization options (size, addons, spice level, etc.)
- **Cart/CartItem** - Shopping cart functionality
- **Order/OrderItem** - Order management with status tracking
- **Payment** - Payment records
- **Notification** - In-app notifications
- **Category** - Menu categorization per merchant

---

## 🔄 Complete System Flow

### **1. User Registration & Authentication Flow**
```
User → Register/Login → JWT Token Generated → Authenticated Requests
                              ↓
                    [AuthController]
                              ↓
                    UserRepository
                              ↓
                    Password Encoded (BCrypt)
```

### **2. Browse & Order Flow (Customer)**
```
Customer App
    ↓
[Browse Merchants] → GET /api/merchants
    ↓
[View Menu] → GET /api/menu/{merchantId}
    ↓
[Add to Cart] → POST /api/cart/items
    ↓ Service Layer
    ├─ CartService.getOrCreateCart()
    ├─ Validate MenuItem exists
    ├─ Add/Update CartItem
    └─ Return updated Cart
    ↓
[Checkout] → POST /api/orders
    ↓ Service Layer
    ├─ OrderService.placeOrder()
    ├─ Calculate total (subtotal + delivery fee)
    ├─ Create Order + OrderItems
    ├─ Process Payment (mock)
    ├─ Notify Merchant
    ├─ Clear Cart
    └─ Return Order confirmation
```

### **3. Order Management Flow (Merchant)**
```
Merchant receives notification
    ↓
GET /api/orders (filtered by merchant)
    ↓
Review Order Details
    ↓
PUT /api/orders/{id}/status
    ├─ PENDING → ACCEPTED
    ├─ ACCEPTED → PREPARING
    └─ PREPARING → OUT_FOR_DELIVERY
    ↓
System notifies customer
```

### **4. Delivery Flow (Driver)**
```
Driver App
    ↓
GET /api/driver/orders (available jobs)
    ↓ Filter: Status = ACCEPTED
    ↓
PUT /api/driver/orders/{id}/accept
    ↓ Service Layer
    ├─ Assign driver to order
    ├─ Update status: OUT_FOR_DELIVERY
    └─ Notify customer
    ↓
Update delivery status
    ├─ OUT_FOR_DELIVERY → DELIVERED
    └─ Customer notified
```

### **5. Admin Oversight**
```
Admin Dashboard
    ↓
GET /api/orders (all orders)
GET /api/users (all users)
Manage merchants, drivers, customers
Monitor system health
```

---

## 🔍 What the System Was Lacking (Before Improvements)

### **❌ Issues Identified:**

1. **No Service Layer**
   - Controllers were directly accessing repositories
   - Business logic mixed with HTTP handling
   - Hard to test, maintain, or extend
   - No transaction management

2. **Tight Coupling**
   - Controllers depended on multiple repositories
   - Difficult to modify business rules
   - Code duplication across controllers

3. **Poor Separation of Concerns**
   - Order placement logic in controller (60+ lines)
   - Cart management mixed with authentication
   - Notification logic scattered

4. **No Transaction Boundaries**
   - Multi-step operations (place order) not atomic
   - Risk of data inconsistency

5. **Limited Error Handling**
   - Exceptions not properly caught/handled
   - Inconsistent error responses

---

## ✅ Improvements Made

### **1. Introduced Service Layer Pattern**

**CartService Interface:**
```java
public interface CartService {
    Cart getOrCreateCart(Long customerId);
    Cart addItemToCart(Long customerId, Long menuItemId, Integer quantity);
    void removeItem(Long itemId);
    void clearCart(Long customerId);
}
```

**OrderService Interface:**
```java
public interface OrderService {
    Order placeOrder(Long customerId, CheckoutRequest request);
    List<Order> getOrdersByUser(Long userId, String userRole);
    Order getOrderById(Long orderId);
    Order updateOrderStatus(Long orderId, OrderStatus newStatus);
    Order assignDriverToOrder(Long orderId, Long driverId);
    List<Order> getAvailableDeliveryJobs();
}
```

### **2. Refactored Controllers**

**Before (OrderController):**
- 150 lines of code
- 7 repository dependencies
- Business logic + HTTP logic mixed

**After (OrderController):**
- 90 lines of code
- 2 dependencies (OrderService, JwtUtil)
- Clean separation: Controller handles HTTP, Service handles business logic

### **3. Added Transaction Management**
```java
@Transactional
public Order placeOrder(Long customerId, CheckoutRequest request) {
    // All-or-nothing operation
    // If any step fails, entire transaction rolls back
}
```

### **4. Improved Error Handling**
```java
try {
    Order order = orderService.placeOrder(customerId, request);
    return ResponseEntity.ok(order);
} catch (IllegalStateException e) {
    return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
} catch (IllegalArgumentException e) {
    return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
}
```

---

## 🎯 Recommended Next Steps

### **High Priority:**

1. **Add More Services**
   - `UserService` - User management, profile updates
   - `MerchantService` - Merchant onboarding, menu management
   - `NotificationService` - Email/SMS/Push notifications
   - `PaymentService` - Real payment gateway integration

2. **Implement DTOs for Responses**
   - Don't expose entities directly
   - Create specific response DTOs for different views

3. **Add Validation**
   - Use `@Valid` annotations
   - Custom validators for business rules

4. **Exception Handler**
   - Global `@ControllerAdvice` for consistent error responses

5. **Testing**
   - Unit tests for services
   - Integration tests for controllers
   - Mock external dependencies

### **Medium Priority:**

6. **Real-time Features**
   - WebSocket for live order tracking
   - Push notifications (Firebase)

7. **Advanced Features**
   - Order scheduling
   - Multiple delivery addresses
   - Promo codes & discounts
   - Ratings & reviews system

8. **Security Enhancements**
   - Role-based method security (`@PreAuthorize`)
   - Rate limiting
   - Input sanitization

### **Future Enhancements:**

9. **Microservices Architecture**
   - Split into: User Service, Order Service, Payment Service, Notification Service

10. **Caching**
    - Redis for frequently accessed data (menu, merchants)

11. **Search & Filters**
    - Elasticsearch for advanced search
    - Filter by cuisine, rating, delivery time

12. **Analytics Dashboard**
    - Sales reports
    - Popular items
    - Driver performance metrics

---

## 📊 Database Schema Overview

```
users (id, name, email, password, phone, role, status, created_at)
merchants (id, owner_id, name, description, image_url, rating, delivery_fee, min_order, is_active, address, banner_url, phone, cuisine_type, opening_hours)
categories (id, merchant_id, name, display_order)
menu_items (id, category_id, name, description, price, image_url, is_available, prep_time_minutes, dietary_tag, popular_score)
item_options (id, menu_item_id, option_group, option_name, price, is_default)
carts (id, customer_id, created_at)
cart_items (id, cart_id, menu_item_id, quantity)
orders (id, customer_id, merchant_id, driver_id, total_amount, delivery_fee, status, delivery_address, payment_status, created_at)
order_items (id, order_id, menu_item_id, quantity, price)
payments (id, order_id, payment_method, transaction_id, amount, status)
notifications (id, user_id, title, message, is_read, created_at)
user_addresses (id, user_id, address_label, full_address, phone, is_default)
```

---

## 🔑 Key Design Patterns Used

1. **Service Layer Pattern** - Business logic separation
2. **Repository Pattern** - Data access abstraction
3. **DTO Pattern** - Data transfer between layers
4. **Dependency Injection** - Loose coupling via constructor injection
5. **Transaction Script** - Managing database transactions
6. **Role-Based Access Control** - Different permissions per user role

---

## 🚀 How to Run

### Backend:
```bash
cd backend-springboot
mvn spring-boot:run
```

### Mobile App:
1. Open `/app` in Android Studio
2. Create `.env` file with `GEMINI_API_KEY`
3. Run on emulator or device

---

## 📝 Conclusion

The system now follows **clean architecture principles** with proper separation of concerns. The new service layer makes the codebase:
- ✅ More maintainable
- ✅ Easier to test
- ✅ Better organized
- ✅ Ready for scaling

**Next focus areas:** Add remaining services, implement comprehensive testing, and integrate real payment/notification systems.
