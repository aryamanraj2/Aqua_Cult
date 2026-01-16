package com.parth.aquasense.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max

/**
 * Utility object for image processing operations
 *
 * Handles:
 * - Converting images to base64 for API upload
 * - Compressing and resizing images
 * - Saving images to internal storage
 */
object ImageUtils {

    /**
     * Convert image URI to base64 string
     *
     * @param imageUri URI of the image (from camera or gallery)
     * @param context Application context
     * @return Base64 encoded string or null if error
     */
    fun imageToBase64(imageUri: Uri, context: Context): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val bytes = inputStream?.readBytes()
            inputStream?.close()

            bytes?.let {
                Base64.encodeToString(it, Base64.NO_WRAP)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Compress and resize image before upload
     *
     * Target specifications:
     * - Max dimension: 1024px on longest side
     * - Format: JPEG
     * - Quality: 80%
     *
     * This reduces upload time and backend processing
     *
     * @param imageUri Original image URI
     * @param context Application context
     * @return URI of compressed image in cache directory, or null if error
     */
    fun compressImage(imageUri: Uri, context: Context): Uri? {
        return try {
            // Load bitmap
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, imageUri)
                ImageDecoder.decodeBitmap(source)
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
            }

            // Calculate new dimensions
            val maxDimension = 1024
            val ratio = maxDimension.toFloat() / max(bitmap.width, bitmap.height)
            val newWidth = (bitmap.width * ratio).toInt()
            val newHeight = (bitmap.height * ratio).toInt()

            // Resize bitmap
            val resized = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)

            // Save compressed image to cache
            val file = File(context.cacheDir, "compressed_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { out ->
                resized.compress(Bitmap.CompressFormat.JPEG, 80, out)
            }

            // Clean up
            if (bitmap != resized) {
                bitmap.recycle()
            }
            resized.recycle()

            Uri.fromFile(file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Convert bitmap to base64 string
     *
     * @param bitmap The bitmap to convert
     * @param quality JPEG compression quality (0-100)
     * @return Base64 encoded string
     */
    fun bitmapToBase64(bitmap: Bitmap, quality: Int = 80): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    /**
     * Save image to app's internal storage
     *
     * @param imageUri Source image URI
     * @param context Application context
     * @return Absolute file path of saved image, or null if error
     */
    fun saveImageToInternalStorage(imageUri: Uri, context: Context): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val fileName = "disease_${System.currentTimeMillis()}.jpg"
            val file = File(context.filesDir, fileName)

            FileOutputStream(file).use { output ->
                inputStream?.copyTo(output)
            }
            inputStream?.close()

            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Delete temporary cached images
     *
     * @param context Application context
     */
    fun clearCachedImages(context: Context) {
        try {
            val cacheDir = context.cacheDir
            cacheDir.listFiles()?.forEach { file ->
                if (file.name.startsWith("compressed_")) {
                    file.delete()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
