package com.example.urbanguard.ui.viewmodel.state

import android.net.Uri

data class CreateReportUiState(
    val isLoading: Boolean = false,
    val titleError: Int? = null,
    val descriptionError: Int? = null,
    val photoUri: Uri? = null,
    val isFormValid: Boolean = false,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val address: String = ""
)