package com.example.urbanguard.domain.usecase

import com.example.urbanguard.domain.model.Report
import com.example.urbanguard.domain.repository.ReportRepository
import javax.inject.Inject

class CreateReportUseCase @Inject constructor(
    private val repository: ReportRepository
) {
    suspend operator fun invoke(
        title: String,
        description: String,
        photoUri: String?
    ): Result<Boolean> {

        var finalPhotoUrl: String? = null

        if (photoUri != null) {
            val uploadResult = repository.uploadPhoto(photoUri)
            uploadResult.onSuccess { url ->
                finalPhotoUrl = url
            }.onFailure {
                return Result.failure(it)
            }
        }

        val newReport = Report(
            title = title,
            description = description,
            photoUrl = finalPhotoUrl
        )


        return repository.createReport(newReport)
    }
}