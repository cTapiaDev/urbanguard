package com.example.urbanguard.data.repository

import com.example.urbanguard.data.local.dao.ReportDao
import com.example.urbanguard.data.mapper.toDomain
import com.example.urbanguard.data.mapper.toDto
import com.example.urbanguard.data.mapper.toEntity
import com.example.urbanguard.data.remote.source.ReportRemoteDataSource
import com.example.urbanguard.domain.model.Report
import com.example.urbanguard.domain.repository.ReportRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import timber.log.Timber
import javax.inject.Inject

class ReportRepositoryImpl @Inject constructor(
    private val reportDao: ReportDao,
    private val remoteDataSource: ReportRemoteDataSource
) : ReportRepository {
    override fun getAllReports(): Flow<List<Report>> {
        return reportDao.getAllReports().map { entities ->
            entities.map { it.toDomain() }
        }
            .onStart { syncReports() }
    }

    private suspend fun syncReports() {
        try {
            val remoteResult = remoteDataSource.fetchReports()
            remoteResult.onSuccess { dtos ->
                val entities = dtos.map { it.toDomain().toEntity() }
                entities.forEach { reportDao.insertReport(it) }
                Timber.d("Sincronización de reportes exitosa")
            }.onFailure {
                Timber.d("Error sincronizando reportes desde la API: ${it.message}")
            }
        } catch (e: Exception) {
            Timber.e("Excepción en syncReports: ${e.message}")
        }
    }

    override suspend fun getReportById(id: String): Report? {
        return reportDao.getReportById(id.toLong())?.toDomain()
    }

    override suspend fun createReport(report: Report): Result<Boolean> {
        return try {
            val remoteResult = remoteDataSource.submitReport(report.toDto())

            if (remoteResult.isSuccess) {
                val savedReportDto = remoteResult.getOrNull()
                savedReportDto?.let {
                    reportDao.insertReport(it.toDomain().toEntity())
                }
                Timber.d("Reporte guardado")
                Result.success(true)
            } else {
                reportDao.insertReport(report.toEntity())
                Timber.d("Estamos sin conexión pero tenemos acceso a Room")
                Result.failure(Exception("Sin conexión: Guardado localmente. Se enviará más tarde."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun uploadPhoto(uri: String): Result<String> {
        return Result.success(uri)
    }
}