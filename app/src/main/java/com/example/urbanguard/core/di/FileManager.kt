package com.example.urbanguard.core.di

import android.content.Context
import android.os.Environment
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun createInternalFile(): File {
        val directory = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("URBAN_RAW_${System.currentTimeMillis()}", ".jpg", directory)
    }
}