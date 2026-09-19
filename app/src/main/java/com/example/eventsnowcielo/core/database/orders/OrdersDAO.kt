package com.example.eventsnowcielo.core.database.orders

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.eventsnowcielo.core.database.orders.entity.OrderEntity
import com.example.eventsnowcielo.core.database.orders.entity.PaymentEntity
import com.example.eventsnowcielo.core.database.orders.entity.PaymentWithOrders
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrders(orders: List<OrderEntity>)

    @Transaction
    suspend fun insertPaymentWithOrders(payment: PaymentEntity, orders: (paymentId: Long) -> List<OrderEntity>) {
        val paymentId = insertPayment(payment)
        val pendingOrders = orders(paymentId)
        insertOrders(pendingOrders)
    }

    // --- Queries returning PaymentWithOrders ---

    @Transaction
    @Query(
        """
        SELECT DISTINCT p.* FROM payments p
        INNER JOIN orders o ON o.paymentId = p.id
        WHERE o.status = :status AND o.eventDate >= :today
        ORDER BY o.eventDate ASC, o.eventTime ASC
        """
    )
    fun getUpcomingOrders(status: String, today: String): Flow<List<PaymentWithOrders>>

    @Transaction
    @Query(
        """
        SELECT DISTINCT p.* FROM payments p
        INNER JOIN orders o ON o.paymentId = p.id
        WHERE o.status = :status AND o.eventDate < :today
        ORDER BY o.eventDate DESC, o.eventTime DESC
        """
    )
    fun getPastOrders(status: String, today: String): Flow<List<PaymentWithOrders>>

    @Query(
        """
        SELECT COUNT(DISTINCT p.id) FROM payments p
        INNER JOIN orders o ON o.paymentId = p.id
        WHERE o.status = :status
        """
    )
    fun getCompletedOrdersCount(status: String): Flow<Int>

    @Transaction
    @Query("SELECT * FROM payments WHERE cieloOrderId = :cieloOrderId")
    suspend fun getPaymentWithOrdersByCieloOrderId(cieloOrderId: String): PaymentWithOrders?

    // --- Updates ---

    @Query("UPDATE orders SET status = :status WHERE paymentId IN (SELECT id FROM payments WHERE cieloOrderId = :cieloOrderId)")
    suspend fun updateOrdersStatusByCieloOrderId(cieloOrderId: String, status: String)

    @Query(
        """
        UPDATE payments 
        SET transactionId = :transactionId,
            email = :email,
            ec = :ec,
            installments = :installments,
            paymentCode = :paymentCode
        WHERE cieloOrderId = :cieloOrderId
        """
    )
    suspend fun updatePaymentInfo(
        cieloOrderId: String,
        transactionId: String?,
        email: String?,
        ec: String?,
        installments: Int?,
        paymentCode: String?
    )

    // --- Transactions ---

    @Transaction
    suspend fun updateAndGetPaymentWithOrders(
        cieloOrderId: String,
        status: String,
        transactionId: String?,
        email: String?,
        ec: String?,
        installments: Int?,
        paymentCode: String?
    ): PaymentWithOrders? {
        updateOrdersStatusByCieloOrderId(cieloOrderId, status)
        updatePaymentInfo(cieloOrderId, transactionId, email, ec, installments, paymentCode)
        return getPaymentWithOrdersByCieloOrderId(cieloOrderId)
    }

    @Transaction
    @Query("SELECT * FROM payments WHERE id = :paymentId")
    suspend fun getRawPaymentWithOrdersByPaymentId(paymentId: Long): PaymentWithOrders?

    @Transaction
    suspend fun getPaymentWithOrdersByPaymentId(paymentId: Long, ticketId: Long): PaymentWithOrders? {
        val result = getRawPaymentWithOrdersByPaymentId(paymentId) ?: return null
        val filteredOrders = result.orders.filter { it.id == ticketId }

        return if (filteredOrders.isNotEmpty()) {
            result.copy(orders = filteredOrders)
        } else {
            null
        }
    }
}