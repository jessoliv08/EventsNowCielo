package com.example.eventsnowcielo.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.eventsnowcielo.core.database.cart.CartDao
import com.example.eventsnowcielo.core.database.cart.CartItemEntity
import com.example.eventsnowcielo.core.database.orders.OrderDao
import com.example.eventsnowcielo.core.database.orders.entity.OrderEntity
import com.example.eventsnowcielo.core.database.orders.entity.PaymentEntity

@Database(
    entities = [
        OrderEntity::class,
        PaymentEntity::class,
        CartItemEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun orderDao(): OrderDao
    abstract fun cartDao(): CartDao
}
