package com.example.vetbook.presentation.screens.vetcare

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vetbook.R
import com.example.vetbook.domain.models.Clinic
import com.example.vetbook.domain.models.Veterinarian
import com.example.vetbook.presentation.theme.Brand
import com.example.vetbook.presentation.viewmodels.VeterinariansViewModel

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
                CircularProgressIndicator(color = Brand)
            } else {
                Text(text = "Doctor not found")
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
            .background(Color.White)
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
                    .height(300.dp)
            ) {
                Image(
                    painter            = painterResource(R.drawable.pawns),
                    contentDescription = null,
                    modifier           = Modifier.fillMaxSize(),
                    contentScale       = ContentScale.Crop
                )
                // Floating back button — Type C HeroHeader
                Box(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(start = 16.dp, top = 12.dp)
                        .size(40.dp)
                        .background(Color.White.copy(alpha = 0.85f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = onBackClick, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint               = Brand
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                // Doctor name and info
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text       = doctor.name,
                            fontSize   = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color      = Color.Black,
                            maxLines   = 1,
                            overflow   = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text     = doctor.specialty,
                            fontSize = 16.sp,
                            color    = Color.Gray,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector        = Icons.Default.Star,
                            contentDescription = null,
                            tint               = Color(0xFFFFC107),
                            modifier           = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text       = "${doctor.rating} (${doctor.reviewsCount} reviews)",
                            fontSize   = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color      = Color.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Statistics row
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatCard(icon = Icons.Default.Person,       value = "116+",          label = "Patients")
                    StatCard(icon = Icons.Default.CheckCircle,  value = "3+",            label = "Years")
                    StatCard(icon = Icons.Default.Star,         value = doctor.rating,   label = "Rating")
                }

                Spacer(modifier = Modifier.height(24.dp))

                // About Me section
                Text(
                    text       = "About Me",
                    fontSize   = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color      = Color.Black
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text       = doctor.bio.ifEmpty { "Experienced veterinarian with a passion for providing the best care for your pets. Specialized in ${doctor.specialty} with ${doctor.experience}." },
                    fontSize   = 14.sp,
                    color      = Color.Gray,
                    lineHeight = 22.sp
                )

                // ── Clinic Location (map) ─────────────────────────────────
                if (clinic != null && clinic.latitude != 0.0 && clinic.longitude != 0.0) {
                    Spacer(modifier = Modifier.height(28.dp))

                    Text(
                        text       = "Clinic Location",
                        fontSize   = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color      = Color.Black
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Address row
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector        = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint               = Brand,
                            modifier           = Modifier.size(18.dp).padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text     = clinic.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color    = Color.Black
                            )
                            if (clinic.address.isNotBlank()) {
                                Text(
                                    text     = clinic.address,
                                    fontSize = 13.sp,
                                    color    = Color.Gray,
                                    lineHeight = 19.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // OpenStreetMap via WebView + Leaflet.js
                    OsmMapView(
                        latitude  = clinic.latitude,
                        longitude = clinic.longitude,
                        label     = clinic.name,
                        modifier  = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(Color(0xFFF0F0F0), RoundedCornerShape(16.dp))
                    )
                }
                // ──────────────────────────────────────────────────────────

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick  = onBookClick,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor = Brand,
                        contentColor   = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text       = "Book Appointment",
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

/**
 * Renders an OpenStreetMap tile via a WebView with Leaflet.js.
 * No API key required. Marker shows [label] in a popup.
 */
@Composable
fun OsmMapView(
    latitude: Double,
    longitude: Double,
    label: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    AndroidView(
        factory = {
            WebView(context).apply {
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    cacheMode = WebSettings.LOAD_NO_CACHE
                }
                webViewClient = WebViewClient()
                clipToOutline = true
            }
        },
        update = { webView ->
            val html = buildOsmHtml(latitude, longitude, label)
            webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
        },
        modifier = modifier
    )
}

private fun buildOsmHtml(lat: Double, lng: Double, label: String): String {
    val safeLabel = label.replace("'", "\\'").replace("\"", "&quot;")
    return """
        <!DOCTYPE html>
        <html>
        <head>
          <meta name="viewport" content="width=device-width, initial-scale=1.0">
          <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
          <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
          <style>
            * { margin: 0; padding: 0; box-sizing: border-box; }
            html, body, #map { width: 100%; height: 100%; }
          </style>
        </head>
        <body>
          <div id="map"></div>
          <script>
            var map = L.map('map', { zoomControl: true, attributionControl: false }).setView([$lat, $lng], 15);
            L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
              maxZoom: 19
            }).addTo(map);
            var marker = L.marker([$lat, $lng]).addTo(map);
            marker.bindPopup('<b>${safeLabel}</b>').openPopup();
          </script>
        </body>
        </html>
    """.trimIndent()
}

@Composable
fun StatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String
) {
    Card(
        modifier = Modifier.width(100.dp),
        shape    = RoundedCornerShape(12.dp),
        colors   = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column(
            modifier              = Modifier.padding(12.dp),
            horizontalAlignment   = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = null,
                tint               = Brand,
                modifier           = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Text(text = label, fontSize = 12.sp, color = Color.Gray)
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
