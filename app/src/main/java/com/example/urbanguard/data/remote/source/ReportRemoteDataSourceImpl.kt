package com.example.urbanguard.data.remote.source

import com.example.urbanguard.data.remote.api.UrbanGuardApiService
import com.example.urbanguard.data.remote.dto.ReportDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ReportRemoteDataSourceImpl @Inject constructor(
    private val apiService: UrbanGuardApiService
) : ReportRemoteDataSource {
    override suspend fun fetchReports(): Result<List<ReportDto>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getReports()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error en el servidor: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun submitReport(reportDto: ReportDto): Result<ReportDto> = withContext(
        Dispatchers.IO) {
        try {
            val response = apiService.createReport(reportDto)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al subir reporte ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun fetchReportById(id: String): Result<ReportDto> {
        TODO("Not yet implemented")
    }
}