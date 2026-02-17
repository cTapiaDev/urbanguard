package com.example.urbanguard.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.urbanguard.R
import com.example.urbanguard.core.di.IoDispatcher
import com.example.urbanguard.ui.viewmodel.state.CreateReportUiEvent
import com.example.urbanguard.ui.viewmodel.state.CreateReportUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateReportViewModel @Inject constructor(
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateReportUiState())
    val uiState: StateFlow<CreateReportUiState> = _uiState.asStateFlow()

    private val _uiEvent = Channel<CreateReportUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private var currentTitle = ""
    private var currentDescription = ""

    fun onTitleChanged(text: String) {
        currentTitle = text
        validateForm()
    }

    fun onDescriptionChanged(text: String) {
        currentDescription = text
        validateForm()
    }

    fun onPhotoSelected(uri: Uri) {
        _uiState.update { it.copy(photoUri = uri) }
        validateForm()
    }

    private fun validateForm() {
        val isTitleValid = currentTitle.length >= 5
        val isDescValid = currentDescription.length >= 10

        _uiState.update { state ->
            state.copy(
                titleError = if (currentTitle.isNotEmpty() && !isTitleValid) R.string.error_title_short else null,
                isFormValid = isTitleValid && isDescValid
            )
        }
    }

    fun submitReport() {
        viewModelScope.launch(dispatcher) {
            _uiState.update { it.copy(isLoading = true) }

            // Simulamos carga de red
            delay(3000)

            val success = true

            if (success) {
                _uiState.update { it.copy(isLoading = false) }
                _uiEvent.send(CreateReportUiEvent.SuccessNavigation)
            } else {
                _uiState.update { it.copy(isLoading = false) }
                _uiEvent.send(CreateReportUiEvent.ShowError("Error al subir reporte"))
            }
        }
    }
}