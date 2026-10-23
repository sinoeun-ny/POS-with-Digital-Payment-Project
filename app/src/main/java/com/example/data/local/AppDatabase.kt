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

                // Insert Standard System Roles
                userDao.insertRole(RoleEntity(roleName = UserRole.CUSTOMER.code, description = UserRole.CUSTOMER.description))
                userDao.insertRole(RoleEntity(roleName = UserRole.MERCHANT.code, description = UserRole.MERCHANT.description))
                userDao.insertRole(RoleEntity(roleName = UserRole.DRIVER.code, description = UserRole.DRIVER.description))
                userDao.insertRole(RoleEntity(roleName = UserRole.ADMIN.code, description = UserRole.ADMIN.description))
            }
        }
    }
}
