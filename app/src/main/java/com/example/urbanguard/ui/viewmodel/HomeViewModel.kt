package com.example.urbanguard.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.urbanguard.domain.model.Report
import com.example.urbanguard.domain.usecase.GetReportsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getReportsUseCase: GetReportsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        fetchReports()
    }

    private fun fetchReports() {
        viewModelScope.launch {
            getReportsUseCase().collect { reports ->
                if (reports.isEmpty()) {
                    _uiState.value = HomeUiState.Empty
                } else {
                    _uiState.value = HomeUiState.Success(reports)
                }
            }
        }
    }

    sealed class HomeUiState {
        object Loading : HomeUiState()
        object Empty : HomeUiState()
        data class Success(val reports: List<Report>) : HomeUiState()
        data class Error(val message: String) : HomeUiState()
    }

}