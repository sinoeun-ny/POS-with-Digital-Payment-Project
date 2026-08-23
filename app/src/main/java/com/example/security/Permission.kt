package com.example.security

enum class Permission(val code: String, val description: String) {
    // Customer
    READ_MERCHANTS("PERM_READ_MERCHANTS", "View nearby merchants and food menus"),
    MANAGE_CART("PERM_MANAGE_CART", "Modify shopping cart items"),
    PLACE_ORDER("PERM_PLACE_ORDER", "Place new food delivery order"),
    MANAGE_ADDRESSES("PERM_MANAGE_ADDRESSES", "Manage personal delivery addresses"),

    // Merchant
    MANAGE_STORE_PROFILE("PERM_MANAGE_STORE_PROFILE", "Update restaurant profile and store hours"),
    MANAGE_MENU("PERM_MANAGE_MENU", "Add, edit, or toggle availability of menu items"),
    FULFILL_ORDERS("PERM_FULFILL_ORDERS", "Accept or reject incoming store orders"),

    // Driver
    VIEW_JOB_BOARD("PERM_VIEW_JOB_BOARD", "Browse available delivery jobs"),
    ACCEPT_DELIVERY("PERM_ACCEPT_DELIVERY", "Claim delivery job and navigate route"),
    UPDATE_DELIVERY_STATUS("PERM_UPDATE_DELIVERY_STATUS", "Broadcast order progress updates"),

    // Admin
    ADMIN_PANEL_ACCESS("PERM_ADMIN_PANEL_ACCESS", "Access centralized admin control dashboard"),
    MANAGE_USERS("PERM_MANAGE_USERS", "View, edit, or suspend user accounts and roles"),
    APPROVE_MERCHANTS("PERM_APPROVE_MERCHANTS", "Approve merchant store onboarding applications"),
    VIEW_SYSTEM_METRICS("PERM_VIEW_SYSTEM_METRICS", "View sales, revenue, and system performance analytics")
}
