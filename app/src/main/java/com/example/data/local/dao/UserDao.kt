package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.data.local.entity.DriverProfileEntity
import com.example.data.local.entity.MerchantProfileEntity
import com.example.data.local.entity.RoleEntity
import com.example.data.local.entity.SessionEntity
import com.example.data.local.entity.UserAddressEntity
import com.example.data.local.entity.UserEntity
import com.example.data.local.entity.UserRoleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE phone = :phone LIMIT 1")
    suspend fun getUserByPhone(phone: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: Long): UserEntity?

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    fun getUserByIdFlow(userId: Long): Flow<UserEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Query("UPDATE users SET fullName = :fullName, phone = :phone, currentActiveRole = :activeRole WHERE id = :userId")
    suspend fun updateUserProfile(userId: Long, fullName: String, phone: String, activeRole: String)

    @Query("SELECT r.roleName FROM roles r INNER JOIN user_roles ur ON r.id = ur.roleId WHERE ur.userId = :userId")
    suspend fun getUserRoleNames(userId: Long): List<String>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRole(role: RoleEntity): Long

    @Query("SELECT * FROM roles WHERE roleName = :roleName LIMIT 1")
    suspend fun getRoleByName(roleName: String): RoleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserRole(userRole: UserRoleEntity)

    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun getAllUsersFlow(): Flow<List<UserEntity>>
}

@Dao
interface UserAddressDao {
    @Query("SELECT * FROM user_addresses WHERE userId = :userId ORDER BY isDefault DESC, id DESC")
    fun getAddressesForUser(userId: Long): Flow<List<UserAddressEntity>>

    @Query("SELECT * FROM user_addresses WHERE userId = :userId AND isDefault = 1 LIMIT 1")
    suspend fun getDefaultAddress(userId: Long): UserAddressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAddress(address: UserAddressEntity): Long

    @Query("UPDATE user_addresses SET isDefault = 0 WHERE userId = :userId")
    suspend fun clearDefaultAddresses(userId: Long)

    @Transaction
    suspend fun setDefaultAddress(userId: Long, addressId: Long) {
        clearDefaultAddresses(userId)
        QuerySetDefault(addressId)
    }

    @Query("UPDATE user_addresses SET isDefault = 1 WHERE id = :addressId")
    suspend fun QuerySetDefault(addressId: Long)

    @Query("DELETE FROM user_addresses WHERE id = :addressId")
    suspend fun deleteAddress(addressId: Long)
}

@Dao
interface MerchantProfileDao {
    @Query("SELECT * FROM merchant_profiles WHERE userId = :userId LIMIT 1")
    suspend fun getMerchantByUserId(userId: Long): MerchantProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMerchantProfile(merchant: MerchantProfileEntity): Long

    @Query("SELECT * FROM merchant_profiles")
    fun getAllMerchantsFlow(): Flow<List<MerchantProfileEntity>>
}

@Dao
interface DriverProfileDao {
    @Query("SELECT * FROM driver_profiles WHERE userId = :userId LIMIT 1")
    suspend fun getDriverByUserId(userId: Long): DriverProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDriverProfile(driver: DriverProfileEntity): Long
}

@Dao
interface SessionDao {
    @Query("SELECT * FROM session WHERE id = 1 LIMIT 1")
    fun getActiveSessionFlow(): Flow<SessionEntity?>

    @Query("SELECT * FROM session WHERE id = 1 LIMIT 1")
    suspend fun getActiveSession(): SessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSession(session: SessionEntity)

    @Query("DELETE FROM session")
    suspend fun clearSession()
}
