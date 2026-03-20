package com.example.vetbook.presentation.screens.pets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.vetbook.domain.models.Vaccination
import com.example.vetbook.domain.models.VaccinationStatus
import com.example.vetbook.presentation.viewmodels.VaccinationViewModel
import com.example.vetbook.presentation.theme.*
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaccinationListScreen(
    petId: String,
    petName: String,
    viewModel: VaccinationViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onAddClick: () -> Unit = {},
    onVaccinationClick: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    // Reload vaccinations whenever the screen resumes (back navigation or add result)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadVaccinations()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Sắp tới", "Quá hạn", "Đã tiêm")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Lịch sử tiêm chủng",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            petName,
                            fontSize = 14.sp,
                            color = HealthMuted
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = HealthPrimary,
                shape = CircleShape
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Thêm tiêm chủng",
                    tint = Color.White
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .padding(paddingValues)
        ) {
            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = HealthPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = HealthPrimary
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    val count = when (index) {
                        0 -> uiState.upcoming.size
                        1 -> uiState.overdue.size
                        2 -> uiState.completed.size
                        else -> 0
                    }

                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(title)
                                if (count > 0) {
                                    Surface(
                                        shape = CircleShape,
                                        color = if (selectedTab == index)
                                            HealthPrimary else HealthMuted
                                    ) {
                                        Text(
                                            text = count.toString(),
                                            modifier = Modifier.padding(
                                                horizontal = 8.dp,
                                                vertical = 2.dp
                                            ),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    )
                }
            }

            // Error message
            if (uiState.error != null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    color = Color(0xFFFFEBEE),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = uiState.error ?: "",
                        modifier = Modifier.padding(16.dp),
                        color = Color(0xFFC62828)
                    )
                }
            }

            // Success message
            if (uiState.successMessage != null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    color = Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = uiState.successMessage ?: "",
                        modifier = Modifier.padding(16.dp),
                        color = Color(0xFF2E7D32)
                    )
                }

                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(3000)
                    viewModel.clearMessages()
                }
            }

            // Content
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = HealthPrimary)
                    }
                }
                else -> {
                    val vaccinations = when (selectedTab) {
                        0 -> uiState.upcoming
                        1 -> uiState.overdue
                        2 -> uiState.completed
                        else -> emptyList()
                    }

                    if (vaccinations.isEmpty()) {
                        EmptyState(
                            tab = selectedTab,
                            onAddClick = onAddClick
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(vaccinations) { vaccination ->
                                VaccinationListItem(
                                    vaccination = vaccination,
                                    onClick = { onVaccinationClick(vaccination.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VaccinationListItem(
    vaccination: Vaccination,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status Icon
            Surface(
                color = when (vaccination.status) {
                    VaccinationStatus.COMPLETED -> Color(0xFFE8F5E9)
                    VaccinationStatus.OVERDUE -> Color(0xFFFFEBEE)
                    VaccinationStatus.SCHEDULED -> Color(0xFFFFF3E0)
                    VaccinationStatus.SKIPPED -> Color(0xFFF5F5F5)
                },
                shape = CircleShape,
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = when (vaccination.status) {
                            VaccinationStatus.COMPLETED -> "✓"
                            VaccinationStatus.OVERDUE -> "!"
                            VaccinationStatus.SCHEDULED -> "○"
                            VaccinationStatus.SKIPPED -> "⊘"
                        },
                        color = when (vaccination.status) {
                            VaccinationStatus.COMPLETED -> Color(0xFF2E7D32)
                            VaccinationStatus.OVERDUE -> Color(0xFFC62828)
                            VaccinationStatus.SCHEDULED -> Color(0xFFEF6C00)
                            VaccinationStatus.SKIPPED -> Color(0xFF757575)
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            // Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = vaccination.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Date
                    Text(
                        text = formatVaccinationDate(vaccination),
                        fontSize = 14.sp,
                        color = HealthMuted
                    )

                    // Type badge
                    Surface(
                        color = HealthSurface,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = when (vaccination.type) {
                                com.example.vetbook.domain.models.VaccinationType.CORE -> "Cốt lõi"
                                com.example.vetbook.domain.models.VaccinationType.NON_CORE -> "Khuyến nghị"
                                com.example.vetbook.domain.models.VaccinationType.OPTIONAL -> "Tùy chọn"
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = HealthPrimary
                        )
                    }
                }

                // Clinic/Vet info
                if (vaccination.clinicName != null || vaccination.veterinarianName != null) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = vaccination.clinicName ?: vaccination.veterinarianName ?: "",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            // Status badge
            Surface(
                color = when (vaccination.status) {
                    VaccinationStatus.COMPLETED -> Color(0xFFE8F5E9)
                    VaccinationStatus.OVERDUE -> Color(0xFFFFEBEE)
                    VaccinationStatus.SCHEDULED -> Color(0xFFFFF3E0)
                    VaccinationStatus.SKIPPED -> Color(0xFFF5F5F5)
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = when (vaccination.status) {
                        VaccinationStatus.COMPLETED -> "Đã tiêm"
                        VaccinationStatus.OVERDUE -> "Quá hạn"
                        VaccinationStatus.SCHEDULED -> "Sắp tới"
                        VaccinationStatus.SKIPPED -> "Bỏ qua"
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = when (vaccination.status) {
                        VaccinationStatus.COMPLETED -> Color(0xFF2E7D32)
                        VaccinationStatus.OVERDUE -> Color(0xFFC62828)
                        VaccinationStatus.SCHEDULED -> Color(0xFFEF6C00)
                        VaccinationStatus.SKIPPED -> Color(0xFF757575)
                    }
                )
            }
        }
    }
}

@Composable
private fun EmptyState(
    tab: Int,
    onAddClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = when (tab) {
                    0 -> "📅"
                    1 -> "⏰"
                    2 -> "✅"
                    else -> "💉"
                },
                fontSize = 64.sp
            )

            Text(
                text = when (tab) {
                    0 -> "Chưa có lịch tiêm sắp tới"
                    1 -> "Không có tiêm chủng quá hạn"
                    2 -> "Chưa có tiêm chủng hoàn thành"
                    else -> "Chưa có dữ liệu"
                },
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Text(
                text = when (tab) {
                    0 -> "Thêm lịch tiêm mới cho thú cưng"
                    1 -> "Tuyệt vời! Bạn đang theo kịp lịch tiêm"
                    2 -> "Bắt đầu theo dõi lịch sử tiêm chủng"
                    else -> ""
                },
                fontSize = 14.sp,
                color = HealthMuted
            )

            if (tab != 1) {
                Button(
                    onClick = onAddClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HealthPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Thêm tiêm chủng")
                }
            }
        }
    }
}

private fun formatVaccinationDate(vaccination: Vaccination): String {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    return when {
        vaccination.completedDate != null -> {
            "Đã tiêm: ${vaccination.completedDate.atZone(java.time.ZoneId.systemDefault()).format(formatter)}"
        }
        vaccination.scheduledDate != null -> {
            "Hẹn: ${vaccination.scheduledDate.atZone(java.time.ZoneId.systemDefault()).format(formatter)}"
        }
        else -> "Chưa xếp lịch"
    }
}