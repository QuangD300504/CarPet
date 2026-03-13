package com.example.vetbook.data.util

import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object PayosHelper {
    const val CLIENT_ID = "c1e69703-56bc-407b-93f0-4b92c68bd1f9"
    const val API_KEY = "ba438550-be67-4788-aed7-c1130d6f1fae"
    private const val PAYOS_CHECKSUM_KEY = "dac94a49bc0a261a47469e7fccbfc6485b284326092fd8898d8199a97c0d0cd2"

    fun calculateSignature(data: Map<String, Any>): String {
        // Sort keys alphabetically
        val sortedKeys = data.keys.sorted()
        
        // Build query string: key1=value1&key2=value2...
        val queryString = sortedKeys.joinToString("&") { key ->
            "$key=${data[key]}"
        }

        return hmacSha256(queryString, PAYOS_CHECKSUM_KEY)
    }

    private fun hmacSha256(data: String, key: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        val secretKey = SecretKeySpec(key.toByteArray(StandardCharsets.UTF_8), "HmacSHA256")
        mac.init(secretKey)
        val hashBytes = mac.doFinal(data.toByteArray(StandardCharsets.UTF_8))
        
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}
