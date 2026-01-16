package com.parth.aquasense.data.remote.api

import com.parth.aquasense.data.remote.dto.ProductDto
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit API interface for Product-related endpoints.
 *
 * All endpoints use the default user ID header for authentication.
 */
interface ProductApi {

    /**
     * Get all unique product categories.
     */
    @GET("products/categories")
    suspend fun getCategories(
        @Header("X-User-ID") userId: String = "default_user_001"
    ): List<String>

    /**
     * Get all products, optionally filtered by category.
     *
     * @param category Optional category filter (feed, medicine, equipment)
     * @param skip Number of products to skip (pagination)
     * @param limit Maximum number of products to return
     */
    @GET("products")
    suspend fun getProducts(
        @Query("category") category: String? = null,
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 100,
        @Header("X-User-ID") userId: String = "default_user_001"
    ): List<ProductDto>

    /**
     * Get a specific product by ID.
     *
     * @param productId The UUID of the product
     */
    @GET("products/{product_id}")
    suspend fun getProductById(
        @Path("product_id") productId: String,
        @Header("X-User-ID") userId: String = "default_user_001"
    ): ProductDto

    /**
     * Search products by name or description.
     *
     * @param query Search query string
     */
    @GET("products/search")
    suspend fun searchProducts(
        @Query("q") query: String,
        @Header("X-User-ID") userId: String = "default_user_001"
    ): List<ProductDto>
}
