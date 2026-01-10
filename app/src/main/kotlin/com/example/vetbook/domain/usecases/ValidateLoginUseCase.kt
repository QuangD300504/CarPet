package com.example.vetbook.domain.usecases

import com.example.vetbook.domain.interfaces.EmailValidator

class ValidateLoginUseCase(
    private val emailValidator: EmailValidator
) {
    
    sealed class ValidationResult {
        object Success : ValidationResult()
        object EmptyFields : ValidationResult()
        object InvalidEmail : ValidationResult()
    }

    operator fun invoke(username: String, password: String): ValidationResult {
        if (username.isBlank() || password.isBlank()) {
            return ValidationResult.EmptyFields
        }
        
        if (!emailValidator.isValid(username)) {
            return ValidationResult.InvalidEmail
        }
        
        return ValidationResult.Success
    }
}
