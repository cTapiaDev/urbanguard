package com.example.urbanguard.core.utils

import android.content.Context
import android.graphics.Bitmap
import timber.log.Timber
import androidx.core.graphics.createBitmap

object MemoryCrusher {

    private val heapSaturator = mutableListOf<Bitmap>()

    fun start(context: Context) {
        try {
            while(true) {
                val bitmap = createBitmap(5000, 5000)
                bitmap.eraseColor(android.graphics.Color.RED)
                heapSaturator.add(bitmap)

                val currentMemory = (heapSaturator.size * 100)
                Timber.tag("BOOTCAMP").e("Memoria capturada: $currentMemory MB")
            }
        } catch (e: OutOfMemoryError) {
            Timber.tag("BOOTCAMP").e("¡CRASH! límite alcanzado")
            throw e
        }
    }

}