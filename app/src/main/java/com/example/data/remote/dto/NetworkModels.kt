package com.example.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MerchantDto(
    @Json(name = "id") val id: Long,
    @Json(name = "name") val name: String,
    @Json(name = "description") val description: String? = null,
    @Json(name = "imageUrl") val imageUrl: String? = null,
    @Json(name = "bannerUrl") val bannerUrl: String? = null,
    @Json(name = "phone") val phone: String? = null,
    @Json(name = "cuisineType") val cuisineType: String? = null,
    @Json(name = "openingHours") val openingHours: String? = null,
    @Json(name = "rating") val rating: Double? = 4.8,
    @Json(name = "reviewsCount") val reviewsCount: Int? = 150,
    @Json(name = "deliveryFee") val deliveryFee: Double? = 1.50,
    @Json(name = "deliveryTimeMins") val deliveryTimeMins: Int? = 20,
    @Json(name = "isOpen") val isOpen: Boolean? = true,
    @Json(name = "address") val address: String? = null,
    @Json(name = "city") val city: String? = "Phnom Penh",
    @Json(name = "menuItems") val menuItems: List<MenuItemDto>? = null
)

@JsonClass(generateAdapter = true)
data class CategoryDto(
    @Json(name = "id") val id: Long,
    @Json(name = "name") val name: String,
    @Json(name = "displayOrder") val displayOrder: Int? = 1
)

@JsonClass(generateAdapter = true)
data class ItemOptionDto(
    @Json(name = "id") val id: Long = 0,
    @Json(name = "optionGroup") val optionGroup: String = "Option",
    @Json(name = "optionName") val optionName: String,
    @Json(name = "priceAdjustment") val priceAdjustment: Double = 0.0,
    @Json(name = "isAvailable") val isAvailable: Boolean = true
)

@JsonClass(generateAdapter = true)
data class MenuItemDto(
    @Json(name = "id") val id: Long,
    @Json(name = "name") val name: String,
    @Json(name = "description") val description: String? = null,
    @Json(name = "price") val price: Double,
    @Json(name = "imageUrl") val imageUrl: String? = null,
    @Json(name = "isAvailable") val isAvailable: Boolean = true,
    @Json(name = "prepTimeMinutes") val prepTimeMinutes: Int? = 15,
    @Json(name = "dietaryTag") val dietaryTag: String? = null,
    @Json(name = "popularScore") val popularScore: Int? = 90,
    @Json(name = "options") val options: List<ItemOptionDto>? = null
)

@JsonClass(generateAdapter = true)
data class LoginRequestDto(
    @Json(name = "email") val email: String,
    @Json(name = "password") val password: String
)

@JsonClass(generateAdapter = true)
data class RegisterRequestDto(
    @Json(name = "name") val name: String,
    @Json(name = "email") val email: String,
    @Json(name = "password") val password: String,
    @Json(name = "phone") val phone: String? = null,
    @Json(name = "role") val role: String? = "CUSTOMER",
    @Json(name = "restaurantName") val restaurantName: String? = null,
    @Json(name = "cuisineType") val cuisineType: String? = null,
    @Json(name = "address") val address: String? = null,
    @Json(name = "city") val city: String? = "Phnom Penh"
)

@JsonClass(generateAdapter = true)
data class AuthResponseDto(
    @Json(name = "token") val token: String,
    @Json(name = "id") val id: Long,
    @Json(name = "name") val name: String,
    @Json(name = "email") val email: String,
    @Json(name = "role") val role: String,
    @Json(name = "merchantId") val merchantId: Long? = null
)

@JsonClass(generateAdapter = true)
data class CartItemRequestDto(
    @Json(name = "menuItemId") val menuItemId: Long,
    @Json(name = "quantity") val quantity: Int,
    @Json(name = "selectedOptions") val selectedOptions: String? = null
)

@JsonClass(generateAdapter = true)
data class CheckoutItemDto(
    @Json(name = "menuItemId") val menuItemId: Long? = null,
    @Json(name = "itemName") val itemName: String? = null,
    @Json(name = "price") val price: Double? = null,
    @Json(name = "quantity") val quantity: Int = 1,
    @Json(name = "selectedOptions") val selectedOptions: String? = null
)

@JsonClass(generateAdapter = true)
data class CheckoutRequestDto(
    @Json(name = "merchantId") val merchantId: Long,
    @Json(name = "deliveryAddress") val deliveryAddress: String,
    @Json(name = "paymentMethod") val paymentMethod: String = "MOCK_KHQR",
    @Json(name = "items") val items: List<CheckoutItemDto>? = null
)

@JsonClass(generateAdapter = true)
data class OrderItemDto(
    @Json(name = "id") val id: Long? = null,
    @Json(name = "menuItemName") val menuItemName: String? = null,
    @Json(name = "quantity") val quantity: Int = 1,
    @Json(name = "price") val price: Double = 0.0,
    @Json(name = "selectedOptions") val selectedOptions: String? = null
)

@JsonClass(generateAdapter = true)
data class OrderDto(
    @Json(name = "id") val id: Long,
    @Json(name = "customerName") val customerName: String? = null,
    @Json(name = "merchantName") val merchantName: String? = null,
    @Json(name = "merchantId") val merchantId: Long? = null,
    @Json(name = "totalAmount") val totalAmount: Double = 0.0,
    @Json(name = "deliveryFee") val deliveryFee: Double = 1.50,
    @Json(name = "status") val status: String = "PLACED",
    @Json(name = "deliveryAddress") val deliveryAddress: String? = null,
    @Json(name = "paymentStatus") val paymentStatus: String? = "PAID",
    @Json(name = "paymentMethod") val paymentMethod: String? = "MOCK_KHQR",
    @Json(name = "itemsSummary") val itemsSummary: String? = null,
    @Json(name = "createdAt") val createdAt: String? = null
)
