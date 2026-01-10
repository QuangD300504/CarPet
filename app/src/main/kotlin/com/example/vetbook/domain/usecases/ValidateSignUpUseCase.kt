package com.example.vetbook.domain.usecases

import com.example.vetbook.domain.interfaces.EmailValidator

class ValidateSignUpUseCase(
    private val emailValidator: EmailValidator
) {
    sealed class ValidationResult {
        object Success : ValidationResult()
        data class Error(val field: Field) : ValidationResult()
    }

    enum class Field {
        FULL_NAME, EMAIL, PHONE, PASSWORD, TERMS
    }

    operator fun invoke(
        fullName: String,
        email: String,
        phoneNumber: String,
        password: String,
        isTermsAccepted: Boolean
    ): ValidationResult {
        // Full Name Validation
        if (fullName.isBlank()) return ValidationResult.Error(Field.FULL_NAME)
        if (fullName.trim().length < 2) return ValidationResult.Error(Field.FULL_NAME)
        if (fullName.any { it.isDigit() || !it.isLetter() && it != ' ' }) return ValidationResult.Error(Field.FULL_NAME)

        // Email Validation
        if (email.isBlank()) return ValidationResult.Error(Field.EMAIL)
        if (!emailValidator.isValid(email)) return ValidationResult.Error(Field.EMAIL)

        // Phone Number Validation
        if (phoneNumber.isBlank()) return ValidationResult.Error(Field.PHONE)
        val cleanPhone = phoneNumber.replace(" ", "")
        if (cleanPhone.length != 10 || !cleanPhone.all { it.isDigit() }) return ValidationResult.Error(Field.PHONE)
        val validPrefixes = listOf("03", "05", "07", "08", "09")
        if (validPrefixes.none { cleanPhone.startsWith(it) }) return ValidationResult.Error(Field.PHONE)

        // Password Validation
        if (password.isBlank()) return ValidationResult.Error(Field.PASSWORD)
        if (password.length < 8) return ValidationResult.Error(Field.PASSWORD)
        if (!password.any { it.isLetter() } || !password.any { it.isDigit() }) return ValidationResult.Error(Field.PASSWORD)

        // Terms Validation
        if (!isTermsAccepted) return ValidationResult.Error(Field.TERMS)

        return ValidationResult.Success
    }
}
