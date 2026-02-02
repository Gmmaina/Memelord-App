package com.memelords.utils

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

object ImageUtils {

    // Compress and resize image
    suspend fun compressImage(
        context: Context,
        uri: Uri,
        maxWidth: Int = 1080,
        maxHeight: Int = 1080,
        quality: Int = 80
    ): File? = withContext(Dispatchers.IO) {
        try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            // Calculate new dimensions
            val ratio: Float = bitmap.width.toFloat() / bitmap.height.toFloat()
            val (newWidth, newHeight) = if (ratio > 1) {
                Pair(maxWidth, (maxWidth / ratio).toInt())
            } else {
                Pair((maxHeight * ratio).toInt(), maxHeight)
            }

            // Resize bitmap
            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)

            // Save to file
            val file = File(context.cacheDir, "compressed_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { out ->
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
            }

            bitmap.recycle()
            resizedBitmap.recycle()

            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Download image to gallery
    suspend fun downloadImageToGallery(
        context: Context,
        imageUrl: String,
        fileName: String = "IMG_${System.currentTimeMillis()}.jpg"
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = java.net.URL(imageUrl)
            val connection = url.openConnection()
            connection.connect()

            val inputStream = connection.getInputStream()
            val bitmap = BitmapFactory.decodeStream(inputStream)

            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }
            }

            val contentResolver: ContentResolver = context.contentResolver
            val uri = contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )

            uri?.let {
                val outputStream: OutputStream? = contentResolver.openOutputStream(it)
                outputStream?.use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                }
            }

            bitmap.recycle()
            inputStream.close()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Share image via intent
    fun shareImage(context: Context, imageUrl: String, caption: String?) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, buildString {
                caption?.let { append("$it\n\n") }
                append(imageUrl)
            })
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share via"))
    }

    // Get file size in MB
    fun getFileSizeMB(file: File): Double {
        return file.length().toDouble() / (1024 * 1024)
    }
}