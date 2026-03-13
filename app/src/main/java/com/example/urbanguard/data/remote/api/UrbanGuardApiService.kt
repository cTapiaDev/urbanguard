package com.example.urbanguard.data.remote.api

import com.example.urbanguard.data.remote.dto.ReportDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface UrbanGuardApiService {

    @GET("reports")
    suspend fun getReports(): Response<List<ReportDto>>

    @POST("reports")
    suspend fun createReport(@Body report: ReportDto): Response<ReportDto>

    @GET("reports/{id}")
    suspend fun getReportById(@Path("id") id: String): Response<ReportDto>
}