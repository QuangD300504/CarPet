package com.example.carpet.domain.usecases

/**
 * Use case for validating sign-up form input.
 */
class ValidateSignUpUseCase {
    
    /**
     * Validation result for sign-up form
     */
    sealed class ValidationResult {
        object Success : ValidationResult()
        data class Error(val message: String) : ValidationResult()
    }

    /**
     * Validates sign-up form fields.
     * @param username The username input
     * @param password The password input
     * @param rePassword The password confirmation input
     * @return ValidationResult indicating success or error with message
     */
    operator fun invoke(
        username: String,
        password: String,
        rePassword: String
    ): ValidationResult {
        return when {
            username.isBlank() || password.isBlank() -> {
                ValidationResult.Error("Fields cannot be empty")
            }
            password != rePassword -> {
                ValidationResult.Error("Passwords do not match")
            }
            password.length < 6 -> {
                ValidationResult.Error("Password must be at least 6 characters")
            }
            else -> {
                ValidationResult.Success
            }
        }
    }
}

