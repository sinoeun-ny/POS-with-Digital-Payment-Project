package com.example.security

enum class UserRole(val code: String, val displayName: String, val description: String) {
    CUSTOMER("ROLE_CUSTOMER", "Customer", "Browse food, place orders, track deliveries"),
    MERCHANT("ROLE_MERCHANT", "Merchant / Restaurant", "Manage store profile, menu items, order fulfillment"),
    DRIVER("ROLE_DRIVER", "Delivery Driver", "Accept delivery jobs, update order progress"),
    ADMIN("ROLE_ADMIN", "System Administrator", "Platform management, user accounts, approvals & analytics");

    companion object {
        fun fromCode(code: String): UserRole {
            return values().find { it.code.equals(code, ignoreCase = true) || it.name.equals(code, ignoreCase = true) }
                ?: CUSTOMER
        }
    }
}
