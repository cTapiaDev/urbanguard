package com.example.urbanguard.domain.model

import java.util.Date

data class Report (
    val id: String = "",
    val title: String,
    val description: String,
    val photoUrl: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val status: ReportStatus = ReportStatus.PENDING,
    val timestamp: Date = Date()
)

enum class ReportStatus {
    PENDING, IN_PROGRESS, RESOLVED
}