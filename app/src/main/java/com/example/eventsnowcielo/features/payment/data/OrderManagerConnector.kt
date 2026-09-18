package com.example.eventsnowcielo.features.payment.data

import android.content.Context
import cielo.orders.domain.Credentials
import cielo.sdk.order.OrderManager
import cielo.sdk.order.ServiceBindListener
import com.example.eventsnowcielo.BuildConfig
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.core.annotation.Single

@Single
class OrderManagerConnector(
    private val context: Context
) {
    private val bindMutex = Mutex()
    private var orderManager: OrderManager? = null
    private var isBound = false
    private var bindDeferred: CompletableDeferred<Unit>? = null

    fun initialize() {
        if (orderManager != null) return

        val credentials = Credentials(
            BuildConfig.CREDENTIALS_CLIENT_ID,
            BuildConfig.CREDENTIALS_ACCESS_TOKEN
        )
        orderManager = OrderManager(credentials, context.applicationContext)
    }

    fun getOrderManager(): OrderManager {
        return orderManager ?: error("OrderManager not initialized. Call initialize() first.")
    }

    suspend fun bind() {
        bindMutex.withLock {
            if (isBound) return

            val pendingBind = bindDeferred
            if (pendingBind != null) {
                pendingBind.await()
                return
            }

            val deferred = CompletableDeferred<Unit>()
            bindDeferred = deferred

            suspendCancellableCoroutine { continuation ->
                getOrderManager().bind(
                    context.applicationContext,
                    object : ServiceBindListener {
                        override fun onServiceBound() {
                            isBound = true
                            deferred.complete(Unit)
                            continuation.resume(Unit)
                        }

                        override fun onServiceBoundError(throwable: Throwable) {
                            bindDeferred = null
                            deferred.completeExceptionally(throwable)
                            continuation.resumeWithException(throwable)
                        }

                        override fun onServiceUnbound() {
                            isBound = false
                            bindDeferred = null
                        }
                    }
                )
            }
        }
    }

    fun unbind() {
        orderManager?.unbind()
        isBound = false
        bindDeferred = null
    }
}
