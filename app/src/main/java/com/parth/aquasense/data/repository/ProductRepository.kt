package com.parth.aquasense.data.repository

import com.parth.aquasense.data.remote.api.ProductApi
import com.parth.aquasense.data.remote.dto.toDomain
import com.parth.aquasense.domain.model.Product
import com.parth.aquasense.domain.util.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for product-related data operations.
 *
 * Handles fetching product data from the API and converting to domain models.
 * All operations are performed on the IO dispatcher for better performance.
 */
@Singleton
class ProductRepository @Inject constructor(
    private val productApi: ProductApi
) {

    /**
     * Get all products, optionally filtered by category.
     *
     * @param category Optional category filter (null returns all products)
     * @return Result containing list of products or error message
     */
    suspend fun getProducts(category: String? = null): Result<List<Product>> =
        withContext(Dispatchers.IO) {
            try {
                val response = productApi.getProducts(category = category)
                Result.Success(response.toDomain())
            } catch (e: Exception) {
                Result.Error(e.message ?: "Failed to fetch products")
            }
        }

    /**
     * Get a specific product by ID.
     *
     * @param productId The UUID of the product
     * @return Result containing the product or error message
     */
    suspend fun getProductById(productId: String): Result<Product> =
        withContext(Dispatchers.IO) {
            try {
                val response = productApi.getProductById(productId)
                Result.Success(response.toDomain())
            } catch (e: Exception) {
                Result.Error(e.message ?: "Failed to fetch product")
            }
        }

    /**
     * Search products by name or description.
     *
     * @param query Search query string
     * @return Result containing list of matching products or error message
     */
    suspend fun searchProducts(query: String): Result<List<Product>> =
        withContext(Dispatchers.IO) {
            try {
                val response = productApi.searchProducts(query)
                Result.Success(response.toDomain())
            } catch (e: Exception) {
                Result.Error(e.message ?: "Failed to search products")
            }
        }

    /**
     * Get all unique product categories.
     *
     * @return Result containing list of category names or error message
     */
    suspend fun getCategories(): Result<List<String>> =
        withContext(Dispatchers.IO) {
            try {
                val response = productApi.getCategories()
                Result.Success(response)
            } catch (e: Exception) {
                Result.Error(e.message ?: "Failed to fetch categories")
            }
        }
}
