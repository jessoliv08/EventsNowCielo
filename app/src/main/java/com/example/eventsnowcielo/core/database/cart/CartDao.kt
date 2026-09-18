package com.example.eventsnowcielo.core.database.cart

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {
    @Query("SELECT * FROM cart_items")
    fun getCartItems(): Flow<List<CartItemEntity>>

    @Query("SELECT * FROM cart_items WHERE eventId = :eventId LIMIT 1")
    suspend fun getItemByEventId(eventId: String): CartItemEntity?

    @Query("SELECT * FROM cart_items")
    suspend fun getCartItemsSnapshot(): List<CartItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateItem(item: CartItemEntity)

    @Query("DELETE FROM cart_items WHERE eventId = :eventId")
    suspend fun deleteItem(eventId: String)

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()
}
