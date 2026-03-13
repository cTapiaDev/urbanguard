package com.example.urbanguard.core.di

import com.example.urbanguard.data.remote.source.ReportRemoteDataSource
import com.example.urbanguard.data.remote.source.ReportRemoteDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {

    @Binds
    @Singleton
    abstract fun bindReportRemoteDataSource(
        impl: ReportRemoteDataSourceImpl
    ): ReportRemoteDataSource
}