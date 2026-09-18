package com.example.eventsnowcielo.features.events.data.remote

import com.example.eventsnowcielo.features.events.data.remote.dto.EventsResponseDto
import retrofit2.http.GET

interface EventApi {
    @GET("api/quick/11aa0896-9000-4510-b393-c1476d7e364b")
    suspend fun getEvents(): EventsResponseDto
}
