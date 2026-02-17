package com.example.urbanguard.domain.usecase

import javax.inject.Inject

class ValidateReportUseCase @Inject constructor() {

    fun execute(title: String, description: String) : ValidationResult {
        if (title.isBlank()) {
            return ValidationResult.Error("El título no puede estar vacío")
        }
        if (title.length < 5) {
            return ValidationResult.Error("El título es muy corto (mínimo 5 letras)")
        }
        if (description.isBlank()) {
            return ValidationResult.Error("La descripción es obligatoria")
        }
        if (description.length < 10) {
            return ValidationResult.Error("Detalla más el problema (mínimo 10 letras)")
        }
        return ValidationResult.Success
    }

    sealed class ValidationResult {
        data object Success: ValidationResult()
        data class Error(val message: String) : ValidationResult()
    }
}