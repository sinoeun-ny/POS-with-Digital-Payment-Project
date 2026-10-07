package com.example.data.remote.api

import com.example.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface MerchantApiService {
    @GET("api/merchants")
    suspend fun getMerchants(@Query("search") search: String? = null): Response<List<MerchantDto>>

    @GET("api/merchants/{id}")
    suspend fun getMerchantById(@Path("id") id: Long): Response<MerchantDto>
}

interface MenuApiService {
    @GET("api/menu")
    suspend fun getMenuItems(
        @Query("merchantId") merchantId: Long? = null,
        @Query("categoryId") categoryId: Long? = null,
        @Query("search") search: String? = null
    ): Response<List<MenuItemDto>>

    @GET("api/menu/{id}")
    suspend fun getMenuItemById(@Path("id") id: Long): Response<MenuItemDto>

    @GET("api/menu/{id}/options")
    suspend fun getItemOptions(@Path("id") id: Long): Response<List<ItemOptionDto>>
}

interface AuthApiService {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequestDto): Response<AuthResponseDto>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequestDto): Response<AuthResponseDto>

    @GET("api/auth/profile")
    suspend fun getProfile(@Header("Authorization") token: String): Response<Map<String, Any>>
}

interface OrderApiService {
    @POST("api/orders")
    suspend fun placeOrder(
        @Header("Authorization") token: String?,
        @Body request: CheckoutRequestDto
    ): Response<OrderDto>

    @GET("api/orders")
    suspend fun getOrders(@Header("Authorization") token: String): Response<List<OrderDto>>

    @GET("api/orders/{id}")
    suspend fun getOrderById(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): Response<OrderDto>

    @PUT("api/orders/{id}/status")
    suspend fun updateOrderStatus(
        @Header("Authorization") token: String,
        @Path("id") id: Long,
        @Body body: Map<String, String>
    ): Response<OrderDto>
}

interface DriverApiService {
    //receive : get order accept by kitchen waiting for pickup
    @GET("api/driver/orders")
    suspend fun getAvailableDeliveryJobs(
        @Header("Authorization") token: String? = null
    ): Response<List<OrderDto>>

    // Accept: driver accept the job
    @PUT("api/driver/orders/{id}/accept")
    suspend fun acceptDeliveryJob(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): Response<OrderDto>

    // Pickup & delivery : update status to OUT_FOR_DELIVERY or Delivered
    @PUT("api/driver/orders/{id}/status")
    suspend fun updateDeliveryStatus(
        @Header("Authorization") token: String? = null,
        @Path("id") id: Long,
        @Body body: Map<String, String>
    ): Response<OrderDto>

}
