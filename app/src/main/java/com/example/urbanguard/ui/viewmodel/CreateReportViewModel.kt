package com.example.urbanguard.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.urbanguard.R
import com.example.urbanguard.core.di.FileManager
import com.example.urbanguard.core.di.IoDispatcher
import com.example.urbanguard.core.utils.ImageProcessor
import com.example.urbanguard.domain.usecase.CreateReportUseCase
import com.example.urbanguard.domain.usecase.ValidateReportUseCase
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
import java.io.File
import javax.inject.Inject

@HiltViewModel
class CreateReportViewModel @Inject constructor(
    private val validateReportUseCase: ValidateReportUseCase,
    private val createReportUseCase: CreateReportUseCase,
    private val fileManager: FileManager,
    private val imageProcessor: ImageProcessor,
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
        val result = validateReportUseCase.execute(currentTitle, currentDescription)
    }

    fun onDescriptionChanged(text: String) {
        currentDescription = text
        validateForm()
    }

    fun onPhotoSelected(context: Context, uri: Uri) {
        viewModelScope.launch(dispatcher) {
            _uiState.update { it.copy(isLoading = true) }

            val interalFile = fileManager.createInternalFile()
            val optimizedPath = imageProcessor.process(context, uri, interalFile)

            _uiState.update { state ->
                state.copy(
                    photoUri = optimizedPath?.let { Uri.fromFile(File(it)) },
                    isLoading = false
                )
            }
            validateForm()
        }
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

            val result = createReportUseCase(
                title = currentTitle,
                description = currentDescription,
                photoUri = _uiState.value.photoUri?.toString()
            )

            result.onSuccess {
                _uiEvent.send(CreateReportUiEvent.SuccessNavigation)
            }.onFailure { error ->
                _uiEvent.send(CreateReportUiEvent.ShowError(error.message ?: "Error al subir reporte"))
            }

            _uiState.update { it.copy(isLoading = false) }
        }
    }
}