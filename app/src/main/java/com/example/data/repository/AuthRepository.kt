package com.example.data.repository

import com.example.data.local.dao.DriverProfileDao
import com.example.data.local.dao.MerchantProfileDao
import com.example.data.local.dao.SessionDao
import com.example.data.local.dao.UserAddressDao
import com.example.data.local.dao.UserDao
import com.example.data.local.entity.DriverProfileEntity
import com.example.data.local.entity.MerchantProfileEntity
import com.example.data.local.entity.SessionEntity
import com.example.data.local.entity.UserAddressEntity
import com.example.data.local.entity.UserEntity
import com.example.data.local.entity.UserRoleEntity
import com.example.security.JwtPayload
import com.example.security.JwtTokenUtil
import com.example.security.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

sealed class AuthResult<out T> {
    data class Success<out T>(val data: T) : AuthResult<T>()
    data class Error(val message: String) : AuthResult<Nothing>()
}

class AuthRepository(
    private val userDao: UserDao,
    private val addressDao: UserAddressDao,
    private val merchantProfileDao: MerchantProfileDao,
    private val driverProfileDao: DriverProfileDao,
    private val sessionDao: SessionDao
) {

    val activeSessionFlow: Flow<SessionEntity?> = sessionDao.getActiveSessionFlow()

    fun getCurrentUserFlow(userId: Long): Flow<UserEntity?> = userDao.getUserByIdFlow(userId)

    fun getUserAddressesFlow(userId: Long): Flow<List<UserAddressEntity>> = addressDao.getAddressesForUser(userId)

    suspend fun login(identifier: String, password: String): AuthResult<SessionEntity> {
        val cleanIdentifier = identifier.trim()
        // Try Spring Boot REST API login if email provided
        if (cleanIdentifier.contains("@")) {
            try {
                val remoteRes = com.example.data.remote.ApiClient.authApi.login(
                    com.example.data.remote.dto.LoginRequestDto(
                        email = cleanIdentifier,
                        password = password
                    )
                )
                if (remoteRes.isSuccessful && remoteRes.body() != null) {
                    val authBody = remoteRes.body()!!
                    var localUser = userDao.getUserByEmail(authBody.email)
                    if (localUser == null) {
                        val newId = userDao.insertUser(
                            UserEntity(
                                id = authBody.id,
                                fullName = authBody.name,
                                phone = "+85512345678",
                                email = authBody.email,
                                passwordHash = password,
                                currentActiveRole = authBody.role
                            )
                        )
                        localUser = userDao.getUserById(newId)
                    }
                    val session = SessionEntity(
                        userId = localUser?.id ?: authBody.id,
                        jwtToken = authBody.token,
                        activeRole = authBody.role
                    )
                    sessionDao.saveSession(session)
                    return AuthResult.Success(session)
                }
            } catch (e: Exception) {
                // Offline fallback to Room
            }
        }

        val user = if (cleanIdentifier.contains("@")) {
            var found = userDao.getUserByEmail(cleanIdentifier.lowercase())
            if (found == null) {
                // If account is one of the database pre-seeded accounts from DataInitializer, auto-create locally
                val lowerEmail = cleanIdentifier.lowercase()
                if (lowerEmail == "customer@example.com" ||
                    lowerEmail == "merchant@example.com" ||
                    lowerEmail == "driver@example.com" ||
                    lowerEmail == "admin@example.com") {
                    val roleCode = when {
                        lowerEmail.contains("admin") -> "ROLE_ADMIN"
                        lowerEmail.contains("merchant") -> "ROLE_MERCHANT"
                        lowerEmail.contains("driver") -> "ROLE_DRIVER"
                        else -> "ROLE_CUSTOMER"
                    }
                    val name = when {
                        lowerEmail.contains("admin") -> "System Admin"
                        lowerEmail.contains("merchant") -> "Bopha Merchant"
                        lowerEmail.contains("driver") -> "Dara Driver"
                        else -> "Sokha Customer"
                    }
                    val newId = userDao.insertUser(
                        UserEntity(
                            fullName = name,
                            email = lowerEmail,
                            phone = "+85512345678",
                            passwordHash = "password123",
                            currentActiveRole = roleCode
                        )
                    )
                    found = userDao.getUserById(newId)
                }
            }
            found
        } else {
            userDao.getUserByPhone(cleanIdentifier)
        }

        if (user == null) {
            return AuthResult.Error("No account found with this email or phone number.")
        }

        if (user.passwordHash != password) {
            return AuthResult.Error("Invalid password. Please check your credentials.")
        }

        val roleNames = userDao.getUserRoleNames(user.id)
        val roles = if (roleNames.isNotEmpty()) {
            roleNames.map { UserRole.fromCode(it) }
        } else {
            listOf(UserRole.fromCode(user.currentActiveRole))
        }

        val token = JwtTokenUtil.generateSimulatedToken(
            userId = user.id,
            email = user.email,
            name = user.fullName,
            roles = roles
        )

        val session = SessionEntity(
            userId = user.id,
            jwtToken = token,
            activeRole = user.currentActiveRole
        )

        sessionDao.saveSession(session)
        return AuthResult.Success(session)
    }

    suspend fun register(
        firstName: String,
        lastName: String,
        phone: String,
        email: String,
        password: String,
        cityProvince: String,
        gender: String,
        selectedRole: UserRole,
        storeName: String = "",
        vehicleType: String = ""
    ): AuthResult<SessionEntity> {
        val fullName = "$firstName $lastName".trim()
        if (fullName.isBlank()) return AuthResult.Error("Please enter your first and last name.")
        if (password.length < 6) return AuthResult.Error("Password must be at least 6 characters.")

        // Check duplicates
        if (phone.isNotBlank() && userDao.getUserByPhone(phone) != null) {
            return AuthResult.Error("An account with phone $phone already exists.")
        }
        if (email.isNotBlank() && userDao.getUserByEmail(email) != null) {
            return AuthResult.Error("An account with email $email already exists.")
        }

        val primaryEmail = if (email.isNotBlank()) email else "user_${System.currentTimeMillis()}@fooddelivery.com"

        // 1. Persist directly to Spring Boot & MySQL DB
        var backendUserId: Long? = null
        var backendToken: String? = null
        try {
            val roleName = when (selectedRole) {
                UserRole.MERCHANT -> "MERCHANT"
                UserRole.DRIVER -> "DRIVER"
                UserRole.ADMIN -> "ADMIN"
                else -> "CUSTOMER"
            }
            val res = com.example.data.remote.ApiClient.authApi.register(
                com.example.data.remote.dto.RegisterRequestDto(
                    name = fullName,
                    email = primaryEmail,
                    password = password,
                    phone = phone,
                    role = roleName,
                    restaurantName = if (storeName.isNotBlank()) storeName else null,
                    address = cityProvince,
                    city = "Phnom Penh"
                )
            )
            if (res.isSuccessful && res.body() != null) {
                backendUserId = res.body()!!.id
                backendToken = res.body()!!.token
                android.util.Log.d("AuthRepository", "Registered user #${backendUserId} in MySQL!")
            }
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Remote register skipped or offline: ${e.message}")
        }

        // 2. Persist locally to Room SQLite
        val newUser = UserEntity(
            id = backendUserId ?: 0,
            fullName = fullName,
            email = primaryEmail,
            phone = phone,
            passwordHash = password,
            currentActiveRole = selectedRole.code
        )

        val userId = userDao.insertUser(newUser)

        // Assign Role
        val roleEntity = userDao.getRoleByName(selectedRole.code)
        if (roleEntity != null) {
            userDao.insertUserRole(UserRoleEntity(userId = userId, roleId = roleEntity.id))
        }

        // Add default address from city / province
        addressDao.insertAddress(
            UserAddressEntity(
                userId = userId,
                label = "Primary Location",
                streetAddress = cityProvince,
                city = cityProvince,
                isDefault = true
            )
        )

        // Onboarding for Merchant or Driver if selected
        if (selectedRole == UserRole.MERCHANT && storeName.isNotBlank()) {
            merchantProfileDao.insertMerchantProfile(
                MerchantProfileEntity(
                    userId = userId,
                    storeName = storeName,
                    category = "Restaurant & Food",
                    description = "Freshly prepared meals and beverages",
                    phone = phone,
                    address = cityProvince
                )
            )
        } else if (selectedRole == UserRole.DRIVER && vehicleType.isNotBlank()) {
            driverProfileDao.insertDriverProfile(
                DriverProfileEntity(
                    userId = userId,
                    vehicleType = vehicleType,
                    licensePlate = "PP-${(1000..9999).random()}",
                    phone = phone
                )
            )
        }

        val token = backendToken ?: JwtTokenUtil.generateSimulatedToken(
            userId = userId,
            email = primaryEmail,
            name = fullName,
            roles = listOf(selectedRole)
        )

        val session = SessionEntity(
            userId = userId,
            jwtToken = token,
            activeRole = selectedRole.code
        )

        sessionDao.saveSession(session)
        return AuthResult.Success(session)
    }

    suspend fun switchUserRole(userId: Long, newRole: UserRole): AuthResult<Unit> {
        val user = userDao.getUserById(userId) ?: return AuthResult.Error("User not found")
        val userRoleNames = userDao.getUserRoleNames(userId)

        // Ensure user has or gets the requested role
        val targetRoleEntity = userDao.getRoleByName(newRole.code)
        if (targetRoleEntity != null && !userRoleNames.contains(newRole.code)) {
            userDao.insertUserRole(UserRoleEntity(userId = userId, roleId = targetRoleEntity.id))
        }

        userDao.updateUserProfile(
            userId = userId,
            fullName = user.fullName,
            phone = user.phone,
            activeRole = newRole.code
        )

        val currentSession = sessionDao.getActiveSession()
        if (currentSession != null) {
            val updatedRoles = (userRoleNames + newRole.code).distinct().map { UserRole.fromCode(it) }
            val newToken = JwtTokenUtil.generateSimulatedToken(
                userId = userId,
                email = user.email,
                name = user.fullName,
                roles = updatedRoles
            )
            sessionDao.saveSession(
                currentSession.copy(
                    jwtToken = newToken,
                    activeRole = newRole.code
                )
            )
        }

        return AuthResult.Success(Unit)
    }

    suspend fun addAddress(userId: Long, label: String, street: String, city: String, isDefault: Boolean) {
        if (isDefault) {
            addressDao.clearDefaultAddresses(userId)
        }
        addressDao.insertAddress(
            UserAddressEntity(
                userId = userId,
                label = label,
                streetAddress = street,
                city = city,
                isDefault = isDefault
            )
        )
    }

    suspend fun deleteAddress(addressId: Long) {
        addressDao.deleteAddress(addressId)
    }

    suspend fun logout() {
        sessionDao.clearSession()
    }
}
