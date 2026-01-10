package com.example.vetbook.utils

import android.util.Patterns
import com.example.vetbook.domain.interfaces.EmailValidator

class AndroidEmailValidator : EmailValidator {
    override fun isValid(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
