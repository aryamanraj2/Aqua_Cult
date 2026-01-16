package com.parth.aquasense.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.parth.aquasense.data.remote.api.AnalysisApi
import com.parth.aquasense.data.remote.api.OrderApi
import com.parth.aquasense.data.remote.api.ProductApi
import com.parth.aquasense.data.remote.api.TankApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Network Module - Provides network dependencies using Hilt
 *
 * Key Concepts:
 * - @Module: Tells Hilt this class provides dependencies
 * - @InstallIn(SingletonComponent::class): These dependencies live as long as the app
 * - @Provides: Marks a function that provides a dependency
 * - @Singleton: Only one instance will be created
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * Base URL for the API
     *
     * IMPORTANT: Change this based on where you're running the app:
     * - Android Emulator: "http://10.0.2.2:8000/api/v1/"
     * - Physical Device: "http://<your-computer-ip>:8000/api/v1/"
     * - Production: "https://your-domain.com/api/v1/"
     */
    private const val BASE_URL = "http://10.0.2.2:8000/api/v1/"

    /**
     * Provides JSON serializer configuration
     *
     * ignoreUnknownKeys: Don't crash if backend sends extra fields
     * isLenient: Be more forgiving with JSON parsing
     */
    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    /**
     * Provides HTTP logging interceptor for debugging
     *
     * This logs all HTTP requests/responses to Logcat
     * Level.BODY: Shows full request/response including headers and body
     */
    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    /**
     * Provides OkHttp client with logging
     *
     * OkHttp is the underlying HTTP engine used by Retrofit
     * We add:
     * - Logging interceptor for debugging
     * - Timeouts to prevent hanging requests
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Provides Retrofit instance
     *
     * Retrofit converts our interface methods into HTTP requests
     * - baseUrl: Where the API is hosted
     * - client: OkHttpClient for making requests
     * - converterFactory: Converts JSON to Kotlin objects using Kotlin Serialization
     */
    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        json: Json
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(
                json.asConverterFactory("application/json".toMediaType())
            )
            .build()
    }

    /**
     * Provides TankApi implementation
     *
     * Retrofit.create() generates the actual implementation of TankApi
     * using the annotations we defined in the interface
     */
    @Provides
    @Singleton
    fun provideTankApi(retrofit: Retrofit): TankApi {
        return retrofit.create(TankApi::class.java)
    }

    /**
     * Provides AnalysisApi implementation
     *
     * Used for disease detection and AI analysis endpoints
     */
    @Provides
    @Singleton
    fun provideAnalysisApi(retrofit: Retrofit): AnalysisApi {
        return retrofit.create(AnalysisApi::class.java)
    }

    /**
     * Provides ProductApi implementation
     *
     * Used for marketplace product endpoints (get products, categories, search)
     */
    @Provides
    @Singleton
    fun provideProductApi(retrofit: Retrofit): ProductApi {
        return retrofit.create(ProductApi::class.java)
    }

    /**
     * Provides OrderApi implementation
     *
     * Used for order management endpoints (create, list, cancel orders)
     */
    @Provides
    @Singleton
    fun provideOrderApi(retrofit: Retrofit): OrderApi {
        return retrofit.create(OrderApi::class.java)
    }
}
