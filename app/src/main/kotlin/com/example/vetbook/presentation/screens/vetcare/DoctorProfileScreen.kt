package com.example.vetbook.presentation.screens.vetcare

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Intent
import android.net.Uri
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.vetbook.R
import com.example.vetbook.domain.models.Clinic
import com.example.vetbook.domain.models.Veterinarian
import com.example.vetbook.presentation.theme.HealthPrimary
import com.example.vetbook.presentation.theme.HealthSurface
import com.example.vetbook.presentation.viewmodels.VeterinariansViewModel
import com.example.vetbook.presentation.components.common.VetBookImage

@Composable
fun DoctorProfileScreen(
    doctorId: String,
    viewModel: VeterinariansViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onBookClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val clinic   by viewModel.clinic.collectAsState()
    val doctor   = uiState.veterinarians.find { it.id == doctorId }

    // Trigger clinic fetch when doctor is resolved
    LaunchedEffect(doctor?.clinicId) {
        doctor?.clinicId?.let { viewModel.fetchClinic(it) }
    }

    if (doctor != null) {
        DoctorProfileContent(
            doctor      = doctor,
            clinic      = clinic,
            onBackClick = onBackClick,
            onBookClick = onBookClick
        )
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (uiState.isLoading) {
                CircularProgressIndicator(color = HealthPrimary)
            } else {
                Text(text = "Không tìm thấy bác sĩ", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
private fun DoctorProfileContent(
    doctor: Veterinarian,
    clinic: Clinic?,
    onBackClick: () -> Unit = {},
    onBookClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HealthSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Doctor hero image with floating back button overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
            ) {
                VetBookImage(
                    model = doctor.imageUrl,
                    contentDescription = doctor.name,
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(0.dp),
                    initials = doctor.initials,
                    fallbackIcon = Icons.Default.Person
                )
                
                // Gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.3f),
                                    Color.Transparent,
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.1f)
                                )
                            )
                        )
                )

                // Floating back button
                Surface(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(start = 16.dp, top = 12.dp)
                        .size(44.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.9f),
                    shadowElevation = 4.dp
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint               = HealthPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-30).dp),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                color = HealthSurface
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    // Doctor name and info
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text       = doctor.name,
                                style      = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2D3142)
                                ),
                                maxLines   = 1,
                                overflow   = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text     = doctor.specialty,
                                style    = MaterialTheme.typography.titleMedium,
                                color    = HealthPrimary,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFFC107).copy(alpha = 0.1f),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector        = Icons.Default.Star,
                                    contentDescription = null,
                                    tint               = Color(0xFFFFC107),
                                    modifier           = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text       = doctor.rating,
                                    style      = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color      = Color(0xFF2D3142)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Statistics row
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            icon = Icons.Default.People,
                            value = "116+",
                            label = "Bệnh nhân",
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            icon = Icons.Default.Verified,
                            value = doctor.experience,
                            label = "Kinh nghiệm",
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            icon = Icons.Default.ChatBubble,
                            value = doctor.reviewsCount.toString(),
                            label = "Nhận xét",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // About Me section
                    Text(
                        text       = "Giới Thiệu",
                        style      = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color      = Color(0xFF2D3142)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text       = doctor.bio.ifEmpty { "Bác sĩ thú y giàu kinh nghiệm với niềm đam mê mang lại sự chăm sóc tốt nhất cho thú cưng của bạn. Chuyên về ${doctor.specialty} với hơn ${doctor.experience} kinh nghiệm." },
                        style      = MaterialTheme.typography.bodyMedium,
                        color      = Color.Gray,
                        lineHeight = 24.sp
                    )

                    // Clinic Location — always show if clinic is non-null
                    if (clinic != null) {
                        Spacer(modifier = Modifier.height(32.dp))

                        Text(
                            text       = "Vị Trí Phòng Khám",
                            style      = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color      = Color(0xFF2D3142)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White,
                            shadowElevation = 2.dp
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.Top) {
                                    Surface(
                                        shape = CircleShape,
                                        color = HealthPrimary.copy(alpha = 0.1f),
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector        = Icons.Default.LocationOn,
                                                contentDescription = null,
                                                tint               = HealthPrimary,
                                                modifier           = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text     = clinic.name,
                                            style    = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color    = Color(0xFF2D3142)
                                        )
                                        if (clinic.address.isNotBlank()) {
                                            Text(
                                                text     = clinic.address,
                                                style    = MaterialTheme.typography.bodySmall,
                                                color    = Color.Gray,
                                                lineHeight = 18.sp
                                            )
                                        }
                                    }
                                }

                                val hasCoords = clinic.latitude != 0.0 && clinic.longitude != 0.0
                                if (hasCoords) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    StaticMapThumbnail(
                                        latitude  = clinic.latitude,
                                        longitude = clinic.longitude,
                                        label     = clinic.name,
                                        address   = clinic.address,
                                        modifier  = Modifier
                                            .fillMaxWidth()
                                            .height(180.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                    )
                                }
                            }
                        }
                    } else if (doctor?.clinicId != null) {
                        // Loading state for clinic
                        Spacer(modifier = Modifier.height(32.dp))
                        Box(
                            modifier = Modifier.fillMaxWidth().height(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = HealthPrimary, modifier = Modifier.size(24.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(40.dp))

                    Button(
                        onClick  = onBookClick,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors   = ButtonDefaults.buttonColors(
                            containerColor = HealthPrimary,
                            contentColor   = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Text(
                            text       = "Đặt Lịch Hẹn",
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

/**
 * Shows a static OpenStreetMap tile as a thumbnail.
 * Tapping it launches Google Maps (or any installed map app) at the given coordinates.
 */
@Composable
fun StaticMapThumbnail(
    latitude: Double,
    longitude: Double,
    label: String,
    address: String = "",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    // Clean Yandex static maps URL with better size
    val staticMapUrl = "https://static-maps.yandex.ru/1.x/?l=map&ll=$longitude,$latitude&z=15&size=650,350&pt=$longitude,$latitude,pm2rdm"

    Box(
        modifier = modifier
            .clickable {
                val query = if (address.isNotBlank()) {
                    Uri.encode("$label, $address")
                } else {
                    Uri.encode(label)
                }
                val geoUri = Uri.parse("geo:$latitude,$longitude?q=$query")
                val intent = Intent(Intent.ACTION_VIEW, geoUri).apply {
                    setPackage("com.google.android.apps.maps")
                }
                if (intent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(intent)
                } else {
                    context.startActivity(Intent(Intent.ACTION_VIEW, geoUri))
                }
            }
            .background(Color(0xFFF3F4F6)),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(staticMapUrl)
                .crossfade(true)
                .build(),
            contentDescription = "Bản đồ",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            error = null // Or use a color/icon fallback if desired
        )
        
        if (staticMapUrl.isBlank()) {
             Icon(
                 imageVector = Icons.Default.LocationOn,
                 contentDescription = null,
                 tint = HealthPrimary.copy(alpha = 0.3f),
                 modifier = Modifier.size(40.dp)
             )
        }
        
        // Gradient for readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.4f)),
                        startY = 100f
                    )
                )
        )
        
        // Tap-to-expand hint with premium styling
        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp),
            shape = RoundedCornerShape(12.dp),
            color = Color.White.copy(alpha = 0.9f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Directions,
                    contentDescription = null,
                    tint = HealthPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Chỉ đường",
                    color = Color.Black,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun StatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(110.dp),
        shape    = RoundedCornerShape(24.dp),
        color    = Color.White,
        shadowElevation = 0.5.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF3F4F6))
    ) {
        Column(
            modifier              = Modifier.padding(12.dp),
            horizontalAlignment   = Alignment.CenterHorizontally,
            verticalArrangement   = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(14.dp),
                color = HealthPrimary.copy(alpha = 0.12f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector        = icon,
                        contentDescription = null,
                        tint               = HealthPrimary,
                        modifier           = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = Color(0xFF111827)
                ),
                maxLines = 2,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DoctorProfileScreenPreview() {
    DoctorProfileContent(
        doctor = Veterinarian(
            id           = "1",
            name         = "Dr. Ali Uzair",
            specialty    = "Cardiologist and Surgeon",
            experience   = "3+ years",
            rating       = "4.9",
            reviewsCount = 95,
            initials     = "DAU",
            bio          = "Experienced cardiologist and surgeon with expertise in treating various cardiac conditions in pets."
        ),
        clinic = Clinic(
            id        = "c1",
            name      = "Happy Paws Clinic",
            address   = "123 Nguyen Hue, Quan 1, Ho Chi Minh City",
            latitude  = 10.7769,
            longitude = 106.7009
        ),
        onBookClick = {}
    )
}
