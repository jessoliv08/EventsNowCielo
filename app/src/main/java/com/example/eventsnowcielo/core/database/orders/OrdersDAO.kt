package com.example.eventsnowcielo.core.database.orders

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrders(orders: List<OrderEntity>)

    @Query(
        """
        SELECT * FROM orders
        WHERE status = :status AND eventDate >= :today
        ORDER BY eventDate ASC, eventTime ASC
        """
    )
    fun getUpcomingOrders(status: String, today: String): Flow<List<OrderEntity>>

    @Query(
        """
        SELECT * FROM orders
        WHERE status = :status AND eventDate < :today
        ORDER BY eventDate DESC, eventTime DESC
        """
    )
    fun getPastOrders(status: String, today: String): Flow<List<OrderEntity>>

    @Query("SELECT COUNT(*) FROM orders WHERE status = :status")
    fun getCompletedOrdersCount(status: String): Flow<Int>

    @Query("UPDATE orders SET status = :status, transactionId = :transactionId WHERE orderId = :orderId")
    suspend fun updateOrderStatus(orderId: String, status: String, transactionId: String?)

    @Query("SELECT * FROM orders WHERE orderId = :orderId")
    suspend fun getOrdersByOrderId(orderId: String): List<OrderEntity>

    @Transaction
    suspend fun updateAndGetOrders(
        orderId: String,
        status: String,
        transactionId: String?
    ): List<OrderEntity> {
        updateOrderStatus(orderId, status, transactionId)
        return getOrdersByOrderId(orderId)
    }
}
