package com.example.urbanguard.domain.repository

import com.example.urbanguard.domain.model.Report
import kotlinx.coroutines.flow.Flow

interface ReportRepository {

    fun getAllReports(): Flow<List<Report>>

    suspend fun getReportById(id: String): Report?

    suspend fun createReport(report: Report): Result<Boolean>

    suspend fun uploadPhoto(uri: String): Result<String>
}