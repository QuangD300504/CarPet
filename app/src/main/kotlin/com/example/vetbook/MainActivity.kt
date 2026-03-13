package com.example.vetbook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.example.vetbook.utils.FirestoreSeeder
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import android.content.Intent
import com.example.vetbook.utils.DeepLinkHandler

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIntent(intent)
        setContent {
            VetBookApp()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val uri = intent?.data
        android.util.Log.d("MainActivity", "Deep link received: $uri")
        if (uri?.scheme == "vetbook-payos") {
            val status = uri.getQueryParameter("status")
            val isSuccess = status == "PAID"
            DeepLinkHandler.emitPaymentResult(isSuccess)
        }
    }
}
