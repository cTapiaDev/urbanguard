package com.example.urbanguard.data.mapper

import com.example.urbanguard.data.local.entity.ReportEntity
import com.example.urbanguard.domain.model.Report
import com.example.urbanguard.domain.model.ReportStatus
import java.util.Date

fun ReportEntity.toDomain(): Report {
    return Report(
        id = this.id.toString(),
        title = this.title,
        description = this.description,
        photoUrl = this.photoUrl,
        latitude = this.latitude,
        longitude = this.longitude,
        status = ReportStatus.valueOf(this.status), // Convierte de String a ENUM
        timestamp = Date(this.timestamp)
    )
}

fun Report.toEntity(): ReportEntity {
    return ReportEntity(
        id = if (this.id.isEmpty()) 0 else this.id.toLong(),
        title = this.title,
        description = this.description,
        photoUrl = this.photoUrl,
        latitude = this.latitude,
        longitude = this.longitude,
        status = this.status.name,
        timestamp = this.timestamp.time
    )
}