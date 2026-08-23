package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.DriverProfileDao
import com.example.data.local.dao.MerchantProfileDao
import com.example.data.local.dao.SessionDao
import com.example.data.local.dao.UserAddressDao
import com.example.data.local.dao.UserDao
import com.example.data.local.entity.CartEntity
import com.example.data.local.entity.CartItemEntity
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.DeliveryEntity
import com.example.data.local.entity.DriverProfileEntity
import com.example.data.local.entity.ItemOptionEntity
import com.example.data.local.entity.MenuItemEntity
import com.example.data.local.entity.MerchantProfileEntity
import com.example.data.local.entity.NotificationEntity
import com.example.data.local.entity.OrderEntity
import com.example.data.local.entity.OrderItemEntity
import com.example.data.local.entity.PaymentEntity
import com.example.data.local.entity.RoleEntity
import com.example.data.local.entity.SessionEntity
import com.example.data.local.entity.UserAddressEntity
import com.example.data.local.entity.UserEntity
import com.example.data.local.entity.UserRoleEntity
import com.example.security.JwtTokenUtil
import com.example.security.UserRole
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        RoleEntity::class,
        UserRoleEntity::class,
        UserAddressEntity::class,
        MerchantProfileEntity::class,
        DriverProfileEntity::class,
        SessionEntity::class,
        CategoryEntity::class,
        MenuItemEntity::class,
        ItemOptionEntity::class,
        CartEntity::class,
        CartItemEntity::class,
        OrderEntity::class,
        OrderItemEntity::class,
        PaymentEntity::class,
        DeliveryEntity::class,
        NotificationEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun addressDao(): UserAddressDao
    abstract fun merchantProfileDao(): MerchantProfileDao
    abstract fun driverProfileDao(): DriverProfileDao
    abstract fun sessionDao(): SessionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smart_food_delivery_db"
                )
                    .addCallback(DatabaseCallback(context.applicationContext))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(private val context: Context) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    val database = getInstance(context)
                    seedDatabase(database)
                }
            }

            private suspend fun seedDatabase(db: AppDatabase) {
                val userDao = db.userDao()
                val addressDao = db.addressDao()
                val merchantDao = db.merchantProfileDao()
                val driverDao = db.driverProfileDao()
                val sessionDao = db.sessionDao()

                // Insert Standard Roles
                val customerRoleId = userDao.insertRole(RoleEntity(roleName = UserRole.CUSTOMER.code, description = UserRole.CUSTOMER.description))
                val merchantRoleId = userDao.insertRole(RoleEntity(roleName = UserRole.MERCHANT.code, description = UserRole.MERCHANT.description))
                val driverRoleId = userDao.insertRole(RoleEntity(roleName = UserRole.DRIVER.code, description = UserRole.DRIVER.description))
                val adminRoleId = userDao.insertRole(RoleEntity(roleName = UserRole.ADMIN.code, description = UserRole.ADMIN.description))

                // Insert Default Demo Customer
                val customerId = userDao.insertUser(
                    UserEntity(
                        fullName = "Sophea Chan",
                        email = "customer@example.com",
                        phone = "+85512345678",
                        passwordHash = "password123",
                        currentActiveRole = UserRole.CUSTOMER.code
                    )
                )
                userDao.insertUserRole(UserRoleEntity(userId = customerId, roleId = customerRoleId))

                addressDao.insertAddress(
                    UserAddressEntity(
                        userId = customerId,
                        label = "Home",
                        streetAddress = "Street 271, Sangkat Takhmao",
                        apartment = "Apt 4B",
                        city = "Phnom Penh",
                        state = "Kandal",
                        postalCode = "12000",
                        isDefault = true
                    )
                )

                // Insert Demo Merchant
                val merchantUserId = userDao.insertUser(
                    UserEntity(
                        fullName = "Kimsour Bistro Admin",
                        email = "merchant@example.com",
                        phone = "+85598765432",
                        passwordHash = "password123",
                        currentActiveRole = UserRole.MERCHANT.code
                    )
                )
                userDao.insertUserRole(UserRoleEntity(userId = merchantUserId, roleId = merchantRoleId))

                merchantDao.insertMerchantProfile(
                    MerchantProfileEntity(
                        userId = merchantUserId,
                        storeName = "Kimsour Khmer Kitchen & Cafe",
                        category = "Asian & Khmer Cuisine",
                        description = "Authentic Lok Lak, Fish Amok & Iced Coffee",
                        phone = "+85598765432",
                        address = "Norodom Blvd, Phnom Penh",
                        rating = 4.9,
                        deliveryFee = 1.50
                    )
                )

                // Insert Demo Driver
                val driverUserId = userDao.insertUser(
                    UserEntity(
                        fullName = "Vichai Express Driver",
                        email = "driver@example.com",
                        phone = "+85588776655",
                        passwordHash = "password123",
                        currentActiveRole = UserRole.DRIVER.code
                    )
                )
                userDao.insertUserRole(UserRoleEntity(userId = driverUserId, roleId = driverRoleId))

                driverDao.insertDriverProfile(
                    DriverProfileEntity(
                        userId = driverUserId,
                        vehicleType = "Motorbike (Honda Dream 125)",
                        licensePlate = "PP-1AB-9988",
                        phone = "+85588776655"
                    )
                )

                // Insert Demo System Admin
                val adminUserId = userDao.insertUser(
                    UserEntity(
                        fullName = "System Administrator",
                        email = "admin@example.com",
                        phone = "+85511223344",
                        passwordHash = "password123",
                        currentActiveRole = UserRole.ADMIN.code
                    )
                )
                userDao.insertUserRole(UserRoleEntity(userId = adminUserId, roleId = adminRoleId))

                // Auto sign in demo customer for initial session
                val token = JwtTokenUtil.generateSimulatedToken(
                    userId = customerId,
                    email = "customer@example.com",
                    name = "Sophea Chan",
                    roles = listOf(UserRole.CUSTOMER)
                )
                sessionDao.saveSession(
                    SessionEntity(
                        userId = customerId,
                        jwtToken = token,
                        activeRole = UserRole.CUSTOMER.code
                    )
                )
            }
        }
    }
}
