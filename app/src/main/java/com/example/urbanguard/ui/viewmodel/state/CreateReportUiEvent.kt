package com.example.urbanguard.ui.viewmodel.state

sealed interface CreateReportUiEvent {
    data class ShowError(val message: String) : CreateReportUiEvent
    data object SuccessNavigation : CreateReportUiEvent
}