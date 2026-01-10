package com.example.vetbook.domain.interfaces

interface EmailValidator {
    fun isValid(email: String): Boolean
}

