package com.example.urbanguard.data.repository

import com.example.urbanguard.data.local.dao.ReportDao
import com.example.urbanguard.data.mapper.toDomain
import com.example.urbanguard.data.mapper.toEntity
import com.example.urbanguard.domain.model.Report
import com.example.urbanguard.domain.repository.ReportRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ReportRepositoryImpl @Inject constructor(
    private val reportDao: ReportDao
) : ReportRepository {
    override fun getAllReports(): Flow<List<Report>> {
        return reportDao.getAllReports().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getReportById(id: String): Report? {
        return reportDao.getReportById(id.toLong())?.toDomain()
    }

    override suspend fun createReport(report: Report): Result<Boolean> {
        return try {
            reportDao.insertReport(report.toEntity())
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun uploadPhoto(uri: String): Result<String> {
        return Result.success(uri)
    }
}