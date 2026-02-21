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

}