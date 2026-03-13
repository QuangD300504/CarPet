package com.example.vetbook.utils

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object CurrencyFormatter {
    private val symbols = DecimalFormatSymbols(Locale.US).apply {
        groupingSeparator = '.'
        decimalSeparator = ','
    }
    private val formatter = DecimalFormat("#,###", symbols)

    /**
     * Formats a number to Vietnamese currency format: 100.000đ
     */
    fun format(amount: Number): String {
        return "${formatter.format(amount.toDouble())}đ"
    }

    /**
     * Formats a string price to Vietnamese currency format.
     * If the string is not a valid number, it returns the original string with 'đ' suffix.
     */
    fun format(amountStr: String): String {
        val amount = amountStr.toDoubleOrNull() ?: return if (amountStr.endsWith("đ")) amountStr else "${amountStr}đ"
        return format(amount)
    }
}
