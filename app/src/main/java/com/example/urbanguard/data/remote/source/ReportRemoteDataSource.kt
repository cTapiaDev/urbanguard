package com.example.urbanguard.data.remote.source

import com.example.urbanguard.data.remote.dto.ReportDto

interface ReportRemoteDataSource {
    suspend fun fetchReports(): Result<List<ReportDto>>
    suspend fun submitReport(reportDto: ReportDto): Result<ReportDto>
    suspend fun fetchReportById(id: String): Result<ReportDto>
}