package com.example.vetbook.presentation.screens.pets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.vetbook.domain.models.Pet
import com.example.vetbook.domain.models.Vaccination
import com.example.vetbook.domain.models.VaccinationStatus
import com.example.vetbook.domain.models.VaccinationType
import com.example.vetbook.presentation.components.pets.VaccineReviewModal
import com.example.vetbook.presentation.components.common.SnackbarType
import com.example.vetbook.presentation.components.common.VetBookSnackbar
import com.example.vetbook.presentation.components.common.VetBookSnackbarHost
import com.example.vetbook.presentation.viewmodels.VaccinationViewModel
import com.example.vetbook.presentation.theme.*
import java.time.Instant
import java.time.format.DateTimeFormatter

// ── Status config ─────────────────────────────────────────────────────────────

private data class StatusStyle(
    val label: String,
    val accentColor: Color,
    val bgColor: Color,
    val textColor: Color
)

private fun statusStyle(status: VaccinationStatus) = when (status) {
    VaccinationStatus.PENDING    -> StatusStyle("Cần đặt lịch", Color(0xFF0D7377), Color(0xFFE6F4F1), Color(0xFF0D5E61))
    VaccinationStatus.SCHEDULED  -> StatusStyle("Đã hẹn",       Color(0xFF1565C0), Color(0xFFE3EAF8), Color(0xFF0D47A1))
    VaccinationStatus.COMPLETED  -> StatusStyle("Đã tiêm",      Color(0xFF2E7D32), Color(0xFFE8F5E9), Color(0xFF1B5E20))
    VaccinationStatus.OVERDUE    -> StatusStyle("Quá hạn",      Color(0xFFC62828), Color(0xFFFFEBEE), Color(0xFFB71C1C))
    VaccinationStatus.SKIPPED    -> StatusStyle("Bỏ qua",       Color(0xFF757575), Color(0xFFF5F5F5), Color(0xFF616161))
}

private fun typeLabel(type: VaccinationType) = when (type) {
    VaccinationType.CORE            -> "Core"
    VaccinationType.REGIONAL        -> "Regional"
    VaccinationType.LIFESTYLE       -> "Lifestyle"
    VaccinationType.NOT_RECOMMENDED -> "Not rec."
    VaccinationType.CUSTOM          -> "Custom"
}

// ── Screen ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaccinationListScreen(
    petId: String,
    petName: String,
    petType: String = "",
    birthDate: Instant? = null,
    viewModel: VaccinationViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onAddClick: () -> Unit = {},
    onVaccinationClick: (String) -> Unit = {},
    onBookAppointment: (vaccinationId: String, doctorId: String) -> Unit = { _, _ -> }
) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val pet = remember(petId, petName, petType, birthDate) {
        Pet(id = petId, name = petName, type = petType, breed = "", birthDate = birthDate)
    }

    LaunchedEffect(uiState.successMessage, uiState.error) {
        uiState.successMessage?.let {
            scope.launch { VetBookSnackbar.show(snackbarHostState, it, SnackbarType.Success) }
            viewModel.clearMessages()
        }
        uiState.error?.let {
            scope.launch { VetBookSnackbar.show(snackbarHostState, it, SnackbarType.Error) }
            viewModel.clearMessages()
        }
    }

    if (uiState.generatedRecords.isNotEmpty()) {
        VaccineReviewModal(
            generatedRecords = uiState.generatedRecords,
            onConfirm = { selected -> viewModel.confirmGeneratedSchedule(selected) },
            onClose = { viewModel.clearGeneratedRecords() }
        )
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.loadVaccinations()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    var selectedTab by remember { mutableStateOf(0) }

    // Tab 0 = Pending + Scheduled, Tab 1 = Overdue, Tab 2 = Completed
    val tabData = listOf(
        "Lịch tiêm" to uiState.upcoming,
        "Quá hạn"   to uiState.overdue,
        "Đã tiêm"   to uiState.completed
    )

    Scaffold(
        snackbarHost = { VetBookSnackbarHost(snackbarHostState) },
        containerColor = Color(0xFFF7F8FA),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Tiêm chủng",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 17.sp,
                            color = Color(0xFF0F1923)
                        )
                        Text(
                            petName,
                            fontSize = 13.sp,
                            color = Color(0xFF8A9BB0)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = Color(0xFF0F1923)
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
                containerColor = Color(0xFF0D7377),
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                elevation = FloatingActionButtonDefaults.elevation(4.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Thêm")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = Color(0xFF0D7377),
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        height = 2.dp,
                        color = Color(0xFF0D7377)
                    )
                },
                divider = {
                    HorizontalDivider(color = Color(0xFFECEFF3), thickness = 1.dp)
                }
            ) {
                tabData.forEachIndexed { index, (title, list) ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        selectedContentColor = Color(0xFF0D7377),
                        unselectedContentColor = Color(0xFF8A9BB0)
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                title,
                                fontSize = 13.sp,
                                fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Normal
                            )
                            if (list.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            if (selectedTab == index) Color(0xFF0D7377)
                                            else Color(0xFFECEFF3)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = list.size.toString(),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedTab == index) Color.White else Color(0xFF8A9BB0)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            when {
                uiState.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF0D7377), strokeWidth = 2.dp)
                    }
                }
                else -> {
                    val vaccinations = tabData[selectedTab].second

                    if (vaccinations.isEmpty()) {
                        EmptyState(
                            tab = selectedTab,
                            pet = pet,
                            onAddClick = onAddClick,
                            onGenerateSchedule = { viewModel.generateSchedule(pet) }
                        )
                    } else {
                        // Group upcoming tab: OVERDUE first, then PENDING, then SCHEDULED
                        val sorted = if (selectedTab == 0) {
                            vaccinations.sortedWith(
                                compareBy {
                                    when (it.status) {
                                        VaccinationStatus.OVERDUE   -> 0
                                        VaccinationStatus.PENDING   -> 1
                                        VaccinationStatus.SCHEDULED -> 2
                                        else -> 3
                                    }
                                }
                            )
                        } else vaccinations

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Series grouping hint for PENDING
                            if (selectedTab == 0 && sorted.any { it.status == VaccinationStatus.PENDING }) {
                                item {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFE6F4F1))
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.CalendarMonth,
                                            contentDescription = null,
                                            tint = Color(0xFF0D7377),
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Text(
                                            "Đặt lịch khám để xếp ngày tiêm cho từng mũi",
                                            fontSize = 12.sp,
                                            color = Color(0xFF0D5E61)
                                        )
                                    }
                                }
                            }

                            items(sorted, key = { it.id }) { vaccination ->
                                VaccinationListItem(
                                    vaccination = vaccination,
                                    onClick = { onVaccinationClick(vaccination.id) },
                                    onBookAppointment = { onBookAppointment(vaccination.id, vaccination.veterinarianId ?: "") }
                                )
                            }

                            item { Spacer(Modifier.height(72.dp)) } // FAB clearance
                        }
                    }
                }
            }
        }
    }
}

// ── Card ──────────────────────────────────────────────────────────────────────

@Composable
private fun VaccinationListItem(
    vaccination: Vaccination,
    onClick: () -> Unit,
    onBookAppointment: () -> Unit
) {
    val style = statusStyle(vaccination.status)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        color = Color.White,
        shadowElevation = 1.dp,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            // Left accent bar
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .background(style.accentColor)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 14.dp, end = 12.dp, top = 12.dp, bottom = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Vaccine name
                    Text(
                        text = vaccination.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF0F1923),
                        modifier = Modifier.weight(1f)
                    )

                    // Status pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(style.bgColor)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = style.label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = style.textColor
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

                // Also known as + type
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    vaccination.alsoKnownAs?.let {
                        Text(it, fontSize = 12.sp, color = Color(0xFF8A9BB0))
                        Text("·", fontSize = 12.sp, color = Color(0xFFCDD4DE))
                    }
                    Text(
                        typeLabel(vaccination.type),
                        fontSize = 11.sp,
                        color = style.accentColor,
                        fontWeight = FontWeight.Medium
                    )
                    if (vaccination.isRecurring) {
                        Text("·", fontSize = 12.sp, color = Color(0xFFCDD4DE))
                        Text("Nhắc lại", fontSize = 11.sp, color = Color(0xFF8A9BB0))
                    }
                }

                // Date line
                val dateText = formatVaccinationDate(vaccination)
                if (dateText.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(dateText, fontSize = 12.sp, color = Color(0xFF8A9BB0))
                }

                // Clinic/vet
                if (vaccination.clinicName != null || vaccination.veterinarianName != null) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = listOfNotNull(vaccination.veterinarianName, vaccination.clinicName).joinToString(" · "),
                        fontSize = 12.sp,
                        color = Color(0xFF8A9BB0)
                    )
                }

                // Action row
                if (vaccination.status == VaccinationStatus.PENDING ||
                    vaccination.status == VaccinationStatus.OVERDUE) {
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { onBookAppointment() },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFF0D7377)
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp, Color(0xFF0D7377).copy(alpha = 0.4f)
                            ),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(
                                Icons.Default.CalendarMonth,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(Modifier.width(5.dp))
                            Text("Đặt lịch", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                if (vaccination.status == VaccinationStatus.SCHEDULED) {
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = onClick, // goes to detail to mark done
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFF2E7D32)
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp, Color(0xFF2E7D32).copy(alpha = 0.4f)
                            ),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(Modifier.width(5.dp))
                            Text("Đã tiêm?", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}

// ── Empty state ───────────────────────────────────────────────────────────────

@Composable
private fun EmptyState(
    tab: Int,
    pet: Pet,
    onAddClick: () -> Unit,
    onGenerateSchedule: () -> Unit
) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Minimal icon instead of giant emoji
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFE6F4F1)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (tab) { 0 -> "💉"; 1 -> "⏱"; else -> "✓" },
                    fontSize = 28.sp
                )
            }

            Spacer(Modifier.height(4.dp))

            Text(
                text = when (tab) {
                    0 -> "Chưa có lịch tiêm"
                    1 -> "Không có quá hạn"
                    2 -> "Chưa có lịch sử tiêm"
                    else -> ""
                },
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF0F1923)
            )

            Text(
                text = when (tab) {
                    0 -> "Tạo lịch theo chuẩn WSAVA cho ${pet.name}"
                    1 -> "Tốt lắm — ${pet.name} đang theo kịp lịch tiêm"
                    2 -> "Lịch sử tiêm của ${pet.name} sẽ hiển thị ở đây"
                    else -> ""
                },
                fontSize = 13.sp,
                color = Color(0xFF8A9BB0),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            if (tab == 0) {
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onGenerateSchedule,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D7377)),
                    shape = RoundedCornerShape(10.dp),
                    enabled = pet.type.isNotBlank(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Text("Tạo lịch tiêm WSAVA", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }

                if (pet.birthDate == null) {
                    Text(
                        "Thêm ngày sinh để xếp lịch chính xác hơn",
                        fontSize = 12.sp,
                        color = Color(0xFFB0894A)
                    )
                }

                TextButton(onClick = onAddClick) {
                    Text("Thêm thủ công", fontSize = 13.sp, color = Color(0xFF8A9BB0))
                }
            }

            if (tab == 2) {
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onAddClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0D7377)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0D7377).copy(alpha = 0.4f))
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Thêm tiêm chủng", fontSize = 13.sp)
                }
            }
        }
    }
}

// ── Date formatter ────────────────────────────────────────────────────────────

private fun formatVaccinationDate(vaccination: Vaccination): String {
    val fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    return when {
        vaccination.completedDate != null ->
            "Đã tiêm ${vaccination.completedDate.atZone(java.time.ZoneId.systemDefault()).format(fmt)}"
        vaccination.scheduledDate != null ->
            "Hẹn ${vaccination.scheduledDate.atZone(java.time.ZoneId.systemDefault()).format(fmt)}"
        else -> ""
    }
}