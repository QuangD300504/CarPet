package com.example.vetbook.utils

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent

object PayosLauncher {
    fun open(context: Context, url: String) {
        try {
            val intent = CustomTabsIntent.Builder()
                .setShowTitle(true)
                .build()
            intent.launchUrl(context, Uri.parse(url))
        } catch (e: Exception) {
            // Fallback to standard browser if Custom Tabs fails
            val browserIntent = android.content.Intent(
                android.content.Intent.ACTION_VIEW,
                Uri.parse(url)
            )
            context.startActivity(browserIntent)
        }
    }
}
