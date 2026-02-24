package com.example.urbanguard.core.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class ImageProcessor @Inject constructor() {

    suspend fun process(context: Context, uri: Uri, dest: File): String? = withContext(Dispatchers.IO) {
        try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }

            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            }

            options.inSampleSize = calculateInSampleSize(options, 1200, 1200)
            options.inJustDecodeBounds = false

            val bitmap = decodeAndRotate(context, uri, options)

            FileOutputStream(dest).use { out ->
                bitmap?.compress(Bitmap.CompressFormat.JPEG, 85, out)
            }
            dest.absolutePath
        } catch (e: Exception) { null }
    }

    private fun decodeAndRotate(context: Context, uri: Uri, options: BitmapFactory.Options): Bitmap? {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val bitmap = BitmapFactory.decodeStream(inputStream, null, options) ?: return null

        val exifInputStream = context.contentResolver.openInputStream(uri) ?: return bitmap
        val exif = ExifInterface(exifInputStream)

        val orientation = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )

        val rotationDegrees = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }

        return if (rotationDegrees != 0) {
            val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } else {
            bitmap
        }
    }

    private fun calculateInSampleSize(opt: BitmapFactory.Options, reqW: Int, reqH: Int): Int {
        var sampleSize = 1
        if (opt.outHeight > reqH || opt.outWidth > reqW) {
            val halfH = opt.outHeight / 2
            val halfW = opt.outWidth / 2
            while (halfH / sampleSize >= reqH && halfW / sampleSize >= reqW) {
                sampleSize *= 2
            }
        }
        return sampleSize
    }
}