package com.example.vetbook.presentation.screens.admin

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.example.vetbook.domain.models.Banner
import com.example.vetbook.presentation.components.topbars.SimpleTopBar
import com.example.vetbook.presentation.theme.Brand
import com.example.vetbook.presentation.viewmodels.admin.AdminBannersViewModel
import com.example.vetbook.presentation.viewmodels.admin.AdminAddEditBannerViewModel
import com.example.vetbook.utils.compressImageForAvatar

// ── Banners List Screen ───────────────────────────────────────────────────────

@Composable
fun AdminBannersScreen(
    viewModel: AdminBannersViewModel = hiltViewModel(),
    onAddBannerClick: () -> Unit,
    onEditBannerClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAddBannerClick, containerColor = Brand, contentColor = Color.White) {
                Icon(Icons.Default.Add, contentDescription = "Add Banner")
            }
        },
        containerColor = Color.White
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when {
                uiState.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center), color = Brand)
                uiState.banners.isEmpty() -> Text(
                    "No banners yet. Tap + to add one.",
                    color = Color.Gray, modifier = Modifier.align(Alignment.Center)
                )
                else -> LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.banners) { banner ->
                        AdminBannerItem(
                            banner = banner,
                            onEditClick = { onEditBannerClick(banner.id) },
                            onDeleteClick = { viewModel.deleteBanner(banner.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminBannerItem(banner: Banner, onEditClick: () -> Unit, onDeleteClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onEditClick),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = banner.imageUrl.ifBlank { "https://via.placeholder.com/80" },
                contentDescription = banner.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(70.dp).clip(RoundedCornerShape(8.dp))
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(banner.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.Black)
                Text(banner.subtitle, fontSize = 12.sp, color = Color.Gray, maxLines = 1)
                Text("Order: ${banner.sortOrder}", fontSize = 11.sp, color = Brand)
            }
            IconButton(onClick = onEditClick) { Icon(Icons.Default.Edit, null, tint = Brand) }
            IconButton(onClick = onDeleteClick) { Icon(Icons.Default.Delete, null, tint = Color.Red) }
        }
    }
}

// ── Add/Edit Banner Screen ────────────────────────────────────────────────────

@Composable
fun AdminAddEditBannerScreen(
    viewModel: AdminAddEditBannerViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onNavigateBackAfterSave: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.onImageSelected(it) }
    }

    LaunchedEffect(uiState.isSuccess) { if (uiState.isSuccess) onNavigateBackAfterSave() }
    LaunchedEffect(uiState.errorMessage) { uiState.errorMessage?.let { snackbar.showSnackbar(it); viewModel.clearError() } }

    Scaffold(
        topBar = { SimpleTopBar(title = if (uiState.id.isBlank()) "Add Banner" else "Edit Banner", onBackClick = onBackClick) },
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = Color.White
    ) { padding ->
        Column(
            Modifier.padding(padding).fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Image picker
            Box(
                Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(12.dp))
                    .background(Color.LightGray.copy(0.3f)).clickable { launcher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (uiState.localImageUri != null || uiState.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = uiState.localImageUri ?: uiState.imageUrl,
                        contentDescription = null, contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AddPhotoAlternate, null, Modifier.size(48.dp), tint = Color.Gray)
                        Text("Tap to select banner image", color = Color.Gray, fontSize = 13.sp)
                    }
                }
            }
            OutlinedTextField(uiState.title, viewModel::onTitleChange, Modifier.fillMaxWidth(), label = { Text("Title") }, singleLine = true)
            OutlinedTextField(uiState.subtitle, viewModel::onSubtitleChange, Modifier.fillMaxWidth(), label = { Text("Subtitle") }, singleLine = true)
            OutlinedTextField(uiState.targetUrl, viewModel::onTargetUrlChange, Modifier.fillMaxWidth(), label = { Text("Target URL (optional)") }, singleLine = true)
            OutlinedTextField(uiState.sortOrder, viewModel::onSortOrderChange, Modifier.fillMaxWidth(), label = { Text("Sort Order") }, singleLine = true)

            Spacer(Modifier.weight(1f))
            Button(
                onClick = {
                    val bytes = uiState.localImageUri?.let { compressImageForAvatar(context, it) }
                    viewModel.saveBanner(bytes)
                },
                Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Brand),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                else Text("Save Banner", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
