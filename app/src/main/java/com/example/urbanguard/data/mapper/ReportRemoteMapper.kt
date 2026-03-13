package com.example.urbanguard.data.mapper

import com.example.urbanguard.data.remote.dto.ReportDto
import com.example.urbanguard.domain.model.Report
import com.example.urbanguard.domain.model.ReportStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun parseApiDate(dateString: String): Date {
    return try {
        Date(dateString.toLong())
    } catch (e: NumberFormatException) {
        try {
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            format.parse(dateString) ?: Date()
        } catch (ex: Exception) {
            Date()
        }
    }
}

fun ReportDto.toDomain(): Report {
    return Report(
        id = this.id ?: "",
        title = this.title,
        description = this.description,
        photoUrl = this.photoUrl,
        latitude = this.latitude,
        longitude = this.longitude,
        status = runCatching { ReportStatus.valueOf(this.status) }.getOrDefault(ReportStatus.PENDING),
        timestamp = parseApiDate(this.timestamp)
    )
}

fun Report.toDto(): ReportDto {
    return ReportDto(
        id = this.id.ifEmpty { null },
        title = this.title,
        description = this.description,
        photoUrl = this.photoUrl,
        latitude = this.latitude,
        longitude = this.longitude,
        status = this.status.name,
        timestamp = this.timestamp.time.toString()
    )
}
