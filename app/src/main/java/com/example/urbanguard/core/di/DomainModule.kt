package com.example.urbanguard.core.di

import com.example.urbanguard.domain.model.Report
import com.example.urbanguard.domain.repository.ReportRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.flowOf
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DomainModule {

    // PROVISIONAL
    @Provides
    @Singleton
    fun provideReportRepository(): ReportRepository {
        return object : ReportRepository {
            override fun getAllReports() = flowOf(emptyList<Report>())
            override suspend fun getReportById(id: String) = null

            override suspend fun createReport(report: Report): Result<Boolean> {
                return Result.success(true)
            }

            override suspend fun uploadPhoto(uri: String): Result<String> {
                return Result.success("https://i.pinimg.com/736x/7f/af/b9/7fafb9d4b589a67f9115f9a258a2b4a4.jpg")
            }

        }
    }
}