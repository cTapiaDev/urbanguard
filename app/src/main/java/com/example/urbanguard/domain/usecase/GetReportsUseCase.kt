package com.example.urbanguard.domain.usecase

import com.example.urbanguard.domain.repository.ReportRepository
import javax.inject.Inject

class GetReportsUseCase @Inject constructor(
    private val repository: ReportRepository
) {
    operator fun invoke() = repository.getAllReports()
}