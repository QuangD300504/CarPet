package com.example.vetbook.presentation.screens.vetcare

import android.annotation.SuppressLint
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.activity.compose.BackHandler
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import com.example.vetbook.utils.DeepLinkHandler

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun InAppPaymentScreen(
    url: String,
    onBack: () -> Unit
) {
    var showCancelDialog by remember { mutableStateOf(false) }

    BackHandler {
        showCancelDialog = true
    }

    var isLoading by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thanh toán PayOS") },
                navigationIcon = {
                    IconButton(onClick = { showCancelDialog = true }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.loadWithOverviewMode = true
                        settings.useWideViewPort = true
                        settings.setSupportMultipleWindows(true)
                        settings.javaScriptCanOpenWindowsAutomatically = true
                        
                        webChromeClient = android.webkit.WebChromeClient()
                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                                isLoading = true
                                url?.let { checkRedirect(it) }
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                isLoading = false
                            }

                            override fun shouldOverrideUrlLoading(
                                view: WebView?,
                                request: WebResourceRequest?
                            ): Boolean {
                                val requestUrl = request?.url?.toString() ?: ""
                                if (checkRedirect(requestUrl)) return true
                                return false
                            }
                            
                            private fun checkRedirect(url: String): Boolean {
                                if (url.startsWith("vetbook-payos://") || 
                                    url.contains("status=PAID") || 
                                    url.contains("status=CANCELLED")) {
                                    
                                    val isSuccess = url.contains("status=PAID")
                                    DeepLinkHandler.emitPaymentResult(isSuccess)
                                    onBack()
                                    return true
                                }
                                return false
                            }
                        }
                        loadUrl(url)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            if (isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().align(androidx.compose.ui.Alignment.TopCenter),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        if (showCancelDialog) {
            AlertDialog(
                onDismissRequest = { showCancelDialog = false },
                title = { Text("Hủy thanh toán") },
                text = { Text("Bạn có chắc chắn muốn hủy quá trình thanh toán này không?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showCancelDialog = false
                            DeepLinkHandler.emitPaymentResult(false)
                            onBack()
                        }
                    ) {
                        Text("Có, hủy", color = Color.Red)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCancelDialog = false }) {
                        Text("Không")
                    }
                },
                containerColor = Color.White
            )
        }
    }
}
