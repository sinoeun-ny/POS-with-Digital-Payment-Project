package com.example.security

object RbacPolicy {

    /**
     * Map each UserRole to its granted Permissions
     */
    val ROLE_PERMISSIONS: Map<UserRole, Set<Permission>> = mapOf(
        UserRole.CUSTOMER to setOf(
            Permission.READ_MERCHANTS,
            Permission.MANAGE_CART,
            Permission.PLACE_ORDER,
            Permission.MANAGE_ADDRESSES
        ),
        UserRole.MERCHANT to setOf(
            Permission.READ_MERCHANTS,
            Permission.MANAGE_STORE_PROFILE,
            Permission.MANAGE_MENU,
            Permission.FULFILL_ORDERS
        ),
        UserRole.DRIVER to setOf(
            Permission.VIEW_JOB_BOARD,
            Permission.ACCEPT_DELIVERY,
            Permission.UPDATE_DELIVERY_STATUS
        ),
        UserRole.ADMIN to setOf(
            Permission.READ_MERCHANTS,
            Permission.MANAGE_CART,
            Permission.PLACE_ORDER,
            Permission.MANAGE_ADDRESSES,
            Permission.MANAGE_STORE_PROFILE,
            Permission.MANAGE_MENU,
            Permission.FULFILL_ORDERS,
            Permission.VIEW_JOB_BOARD,
            Permission.ACCEPT_DELIVERY,
            Permission.UPDATE_DELIVERY_STATUS,
            Permission.ADMIN_PANEL_ACCESS,
            Permission.MANAGE_USERS,
            Permission.APPROVE_MERCHANTS,
            Permission.VIEW_SYSTEM_METRICS
        )
    )

    fun hasPermission(userRoles: List<UserRole>, permission: Permission): Boolean {
        return userRoles.any { role ->
            ROLE_PERMISSIONS[role]?.contains(permission) == true
        }
    }

    fun getPermissionsForRoles(userRoles: List<UserRole>): Set<Permission> {
        val permissions = mutableSetOf<Permission>()
        userRoles.forEach { role ->
            ROLE_PERMISSIONS[role]?.let { permissions.addAll(it) }
        }
        return permissions
    }
}
