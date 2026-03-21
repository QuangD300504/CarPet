package com.example.vetbook

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.example.vetbook.utils.FirestoreSeeder
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.vetbook.presentation.viewmodels.SharedNotificationViewModel
import com.example.vetbook.utils.DeepLinkHandler
import com.example.vetbook.presentation.viewmodels.SharedNotificationViewModel.Companion.permissionResultChannel
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
        != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
            1001
        )
    }
}
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == SharedNotificationViewModel.REQUEST_NOTIFICATION_PERMISSION) {
            val granted = grantResults.isNotEmpty() &&
                    grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED
            lifecycleScope.launch {
                permissionResultChannel.send(granted)
            }
        }
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
