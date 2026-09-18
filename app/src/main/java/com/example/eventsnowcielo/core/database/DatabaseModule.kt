package com.example.eventsnowcielo.core.database

import androidx.room.Room
import com.example.eventsnowcielo.core.database.cart.CartDao
import com.example.eventsnowcielo.core.database.orders.OrderDao
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
class DatabaseModule {

    @Single
    fun provideAppDatabase(context: android.content.Context): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "events_now_cielo.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Single
    fun provideOrderDao(database: AppDatabase): OrderDao = database.orderDao()

    @Single
    fun provideCartDao(database: AppDatabase): CartDao = database.cartDao()
}
