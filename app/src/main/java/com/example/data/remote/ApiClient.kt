package com.example.data.remote

import android.content.Context
import android.content.SharedPreferences
import com.example.data.remote.api.AuthApiService
import com.example.data.remote.api.DriverApiService
import com.example.data.remote.api.MenuApiService
import com.example.data.remote.api.MerchantApiService
import com.example.data.remote.api.OrderApiService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    // Default to the user's computer Wi-Fi IP so the physical phone can reach Spring Boot on the LAN!
    // Emulator alternative is http://10.0.2.2:8080/
    const val DEFAULT_USB_URL = "http://127.0.0.1:8080/"
    const val DEFAULT_PHONE_WIFI_URL = "http://192.168.1.28:8080/"
    const val DEFAULT_EMULATOR_URL = "http://10.0.2.2:8080/"
    private const val PREFS_NAME = "foodeats_network_prefs"
    private const val KEY_BASE_URL = "backend_base_url"

    private var currentBaseUrl = DEFAULT_USB_URL
    private var retrofit: Retrofit? = null

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .writeTimeout(8, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        currentBaseUrl = prefs.getString(KEY_BASE_URL, DEFAULT_USB_URL) ?: DEFAULT_USB_URL
        rebuildRetrofit()
    }

    fun getBaseUrl(): String = currentBaseUrl

    fun setBaseUrl(context: Context, newUrl: String) {
        var formatted = newUrl.trim()
        if (!formatted.startsWith("http://") && !formatted.startsWith("https://")) {
            formatted = "http://$formatted"
        }
        if (!formatted.endsWith("/")) {
            formatted = "$formatted/"
        }
        currentBaseUrl = formatted
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_BASE_URL, formatted).apply()
        rebuildRetrofit()
    }

    private fun rebuildRetrofit() {
        retrofit = Retrofit.Builder()
            .baseUrl(currentBaseUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    private fun getRetrofit(): Retrofit {
        if (retrofit == null) {
            rebuildRetrofit()
        }
        return retrofit!!
    }

    val merchantApi: MerchantApiService
        get() = getRetrofit().create(MerchantApiService::class.java)

    val menuApi: MenuApiService
        get() = getRetrofit().create(MenuApiService::class.java)

    val authApi: AuthApiService
        get() = getRetrofit().create(AuthApiService::class.java)

    val orderApi: OrderApiService
        get() = getRetrofit().create(OrderApiService::class.java)

    val driverApi: DriverApiService
        get() = getRetrofit().create(DriverApiService::class.java)

    //connection
    val DEFAULT_MODE = ConnectionMode.USB

    //switch mode dynamically
    fun applyConnectionMode(context: Context, mode: ConnectionMode){
        setBaseUrl(context, mode.defaultUrl)
    }
    suspend fun testConnection(): Pair<Boolean, String> {
        return try {
            val response = merchantApi.getMerchants()
            if (response.isSuccessful) {
                val count = response.body()?.size ?: 0
                Pair(true, "Connected to FoodEats Backend ($count merchants found)")
            } else {
                Pair(false, "Server responded with HTTP ${response.code()}")
            }
        } catch (e: Exception) {
            Pair(false, "Cannot connect to $currentBaseUrl: ${e.localizedMessage ?: "Timeout"}")
        }
    }
}
