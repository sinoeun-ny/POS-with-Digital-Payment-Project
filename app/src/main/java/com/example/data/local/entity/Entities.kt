package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val email: String,
    val phone: String,
    val passwordHash: String,
    val isVerified: Boolean = true,
    val currentActiveRole: String = "ROLE_CUSTOMER",
    val profilePicUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "roles")
data class RoleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val roleName: String, // e.g., ROLE_CUSTOMER, ROLE_MERCHANT, ROLE_DRIVER, ROLE_ADMIN
    val description: String
)

@Entity(
    tableName = "user_roles",
    primaryKeys = ["userId", "roleId"],
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = RoleEntity::class,
            parentColumns = ["id"],
            childColumns = ["roleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"]), Index(value = ["roleId"])]
)
data class UserRoleEntity(
    val userId: Long,
    val roleId: Long
)

@Entity(
    tableName = "user_addresses",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"])]
)
data class UserAddressEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val label: String, // Home, Work, Gym, Parents, etc.
    val streetAddress: String,
    val apartment: String = "",
    val city: String,
    val state: String = "CA",
    val postalCode: String = "90210",
    val isDefault: Boolean = false,
    val latitude: Double = 37.7749,
    val longitude: Double = -122.4194
)

@Entity(
    tableName = "merchant_profiles",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"])]
)
data class MerchantProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val storeName: String,
    val category: String, // Asian, Burger, Pizza, Cafe, Dessert
    val description: String,
    val phone: String,
    val address: String,
    val rating: Double = 4.8,
    val deliveryFee: Double = 2.99,
    val estimatedMinutes: String = "20-30 min",
    val isApproved: Boolean = true,
    val isOpen: Boolean = true
)

@Entity(
    tableName = "driver_profiles",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"])]
)
data class DriverProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val vehicleType: String, // Motorbike, Bicycle, Scooter, Car
    val licensePlate: String,
    val phone: String,
    val isVerified: Boolean = true,
    val isAvailable: Boolean = true,
    val totalDeliveries: Int = 124,
    val rating: Double = 4.9
)

@Entity(tableName = "session")
data class SessionEntity(
    @PrimaryKey val id: Int = 1,
    val userId: Long,
    val jwtToken: String,
    val activeRole: String,
    val loggedInAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "categories",
    foreignKeys = [
        ForeignKey(
            entity = MerchantProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["merchantId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["merchantId"])]
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val merchantId: Long,
    val categoryName: String,
    val displayOrder: Int = 0
)

@Entity(
    tableName = "menu_items",
    foreignKeys = [
        ForeignKey(
            entity = MerchantProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["merchantId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["merchantId"])]
)
data class MenuItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val merchantId: Long,
    val categoryName: String,
    val name: String,
    val description: String,
    val price: Double,
    val imageUrl: String = "",
    val isAvailable: Boolean = true,
    val popularScore: Int = 0
)

@Entity(
    tableName = "item_options",
    foreignKeys = [
        ForeignKey(
            entity = MenuItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["menuItemId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["menuItemId"])]
)
data class ItemOptionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val menuItemId: Long,
    val optionGroup: String, // e.g., Size, Sweetness, Extras
    val optionName: String, // e.g., Large, 50% Sugar, Extra Boba
    val priceAdjustment: Double = 0.0
)

@Entity(
    tableName = "carts",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"])]
)
data class CartEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val merchantId: Long? = null,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "cart_items",
    foreignKeys = [
        ForeignKey(
            entity = CartEntity::class,
            parentColumns = ["id"],
            childColumns = ["cartId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["cartId"])]
)
data class CartItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cartId: Long,
    val menuItemId: Long,
    val itemName: String,
    val price: Double,
    val quantity: Int,
    val selectedOptions: String = "" // JSON or comma-separated options
)

@Entity(
    tableName = "orders",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index(value = ["userId"])]
)
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderNumber: String,
    val userId: Long,
    val merchantId: Long,
    val merchantName: String,
    val driverId: Long? = null,
    val status: String, // PENDING, ACCEPTED, PREPARING, OUT_FOR_DELIVERY, DELIVERED, CANCELLED
    val subtotal: Double,
    val deliveryFee: Double,
    val totalAmount: Double,
    val deliveryAddress: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "order_items",
    foreignKeys = [
        ForeignKey(
            entity = OrderEntity::class,
            parentColumns = ["id"],
            childColumns = ["orderId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["orderId"])]
)
data class OrderItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderId: Long,
    val menuItemName: String,
    val price: Double,
    val quantity: Int,
    val optionsSummary: String = ""
)

@Entity(
    tableName = "payments",
    foreignKeys = [
        ForeignKey(
            entity = OrderEntity::class,
            parentColumns = ["id"],
            childColumns = ["orderId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["orderId"])]
)
data class PaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderId: Long,
    val paymentMethod: String, // MOCK_KHQR, MOCK_CARD, CASH_ON_DELIVERY
    val transactionRef: String,
    val amount: Double,
    val status: String, // SUCCESS, PENDING, FAILED
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "deliveries",
    foreignKeys = [
        ForeignKey(
            entity = OrderEntity::class,
            parentColumns = ["id"],
            childColumns = ["orderId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["orderId"])]
)
data class DeliveryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderId: Long,
    val driverId: Long,
    val status: String, // ASSIGNED, PICKED_UP, DELIVERED
    val pickupTime: Long? = null,
    val deliveredTime: Long? = null
)

@Entity(
    tableName = "notifications",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"])]
)
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val title: String,
    val message: String,
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
