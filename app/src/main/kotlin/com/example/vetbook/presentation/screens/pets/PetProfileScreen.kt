package com.example.vetbook.presentation.screens.pets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil3.compose.AsyncImage
import com.example.vetbook.domain.models.Pet
import com.example.vetbook.domain.models.VaccinationStatus
import com.example.vetbook.presentation.viewmodels.PetProfileViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import com.example.vetbook.presentation.theme.HealthPrimary
import com.example.vetbook.presentation.theme.HealthSurface
import com.example.vetbook.presentation.theme.TextPrimary
import com.example.vetbook.presentation.theme.TextSecondary
import com.example.vetbook.presentation.theme.HealthMuted
import com.example.vetbook.presentation.theme.Background

// ── Age helper ────────────────────────────────────────────────────────────────

/**
 * Computes a Vietnamese age string from a birthDate Instant.
 * FIX: pet.age is always "" because AddPetViewModel sets age="" and never
 * writes it. We now derive age from pet.birthDate instead.
 */
private fun computePetAge(birthDate: Instant?): String {
    if (birthDate == null) return "-"
    val birth = birthDate.atZone(ZoneId.systemDefault()).toLocalDate()
    val today = LocalDate.now()
    if (birth.isAfter(today)) return "-"
    val years  = ChronoUnit.YEARS.between(birth, today).toInt()
    val months = ChronoUnit.MONTHS.between(birth.plusYears(years.toLong()), today).toInt()
    return when {
        years > 0 && months > 0 -> "$years năm $months tháng"
        years > 0               -> "$years năm"
        months > 0              -> "$months tháng"
        else                    -> "< 1 tháng"
    }
}

// ── Screen ────────────────────────────────────────────────────────────────────

@Composable
fun PetProfileScreen(
    petId: String,
    viewModel: PetProfileViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onEditClick: (String) -> Unit = {},
    onDeleted: () -> Unit = {},
    onVaccinationsViewAll: (petId: String, petName: String, petType: String, birthDate: Instant?) -> Unit = { _, _, _, _ -> },
    onVaccinationClick: (vaccinationId: String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Xác nhận xóa", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Bạn có chắc chắn muốn xóa hồ sơ của ${uiState.pet?.name ?: "thú cưng"} không? " +
                    "Thao tác này không thể hoàn tác."
                )
            },
            confirmButton = {
                TextButton(onClick = { showDeleteDialog = false; viewModel.deletePet(onDeleted) }) {
                    Text("Xóa", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Hủy", color = TextSecondary)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp)
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(Background)) {
        when {
            uiState.isLoading -> CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = HealthPrimary
            )
            uiState.error != null -> Text(
                text = uiState.error ?: "Không thể tải hồ sơ thú cưng",
                modifier = Modifier.align(Alignment.Center),
                color = Color.Red
            )
            uiState.pet != null -> PetProfileContent(
                pet = uiState.pet!!,
                onBackClick = onBackClick,
                onEditClick = { onEditClick(petId) },
                onDeleteClick = { showDeleteDialog = true },
                onVaccinationsViewAll = onVaccinationsViewAll,
                onVaccinationClick = onVaccinationClick
            )
        }
    }
}

// ── Content ───────────────────────────────────────────────────────────────────

@Composable
private fun PetProfileContent(
    pet: Pet,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onVaccinationsViewAll: (String, String, String, Instant?) -> Unit,
    onVaccinationClick: (String) -> Unit
) {
    // Compute age once; recomputes only when birthDate changes.
    val ageDisplay = remember(pet.birthDate) { computePetAge(pet.birthDate) }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {

        // ── Hero image ────────────────────────────────────────────────────────
        Box(modifier = Modifier.fillMaxWidth().height(360.dp)) {
            AsyncImage(
                model = pet.realImgUrl?.ifBlank {
                    "https://images.unsplash.com/photo-1543466835-00a7907e9de1?q=80&w=1974&auto=format&fit=crop"
                } ?: "https://images.unsplash.com/photo-1543466835-00a7907e9de1?q=80&w=1974&auto=format&fit=crop",
                contentDescription = pet.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth().height(100.dp)
                    .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.4f), Color.Transparent)))
            )
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.padding(20.dp).background(Color.White.copy(alpha = 0.25f), CircleShape)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại", tint = Color.White)
            }
        }

        // ── Detail card ───────────────────────────────────────────────────────
        Surface(
            modifier = Modifier.fillMaxWidth().offset(y = (-40).dp),
            shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
            color = Background
        ) {
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 36.dp)) {

                // Name + gender badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = pet.name,
                            fontSize = 34.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary,
                            letterSpacing = (-0.5).sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "${pet.type} • ${pet.breed.ifBlank { "Giống loài khác" }}",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Medium,
                            color = HealthMuted
                        )
                    }
                    Surface(color = HealthSurface, shape = RoundedCornerShape(14.dp)) {
                        Text(
                            text = when (pet.gender) {
                                "Male", "Đực"   -> "Đực"
                                "Female", "Cái" -> "Cái"
                                else             -> "N/A"
                            },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = HealthPrimary
                        )
                    }
                }

                Spacer(Modifier.height(36.dp))

                // Stats — age uses computePetAge(birthDate), NOT pet.age
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatBox(
                        label = "Cân nặng",
                        value = pet.weight.let { if (it.isBlank()) "-" else "$it kg" }
                    )
                    StatBox(label = "Tuổi", value = ageDisplay)
                    StatBox(
                        label = "Loại",
                        value = when (pet.type) {
                            "Dog", "Chó"   -> "Chó"
                            "Cat", "Mèo"   -> "Mèo"
                            "Bird", "Chim" -> "Chim"
                            else            -> pet.type
                        }
                    )
                }

                Spacer(Modifier.height(36.dp))

                // Note
                if (pet.note.isNotBlank()) {
                    Section(title = "Ghi chú y tế") {
                        Text(text = pet.note, fontSize = 15.sp, color = TextSecondary, lineHeight = 26.sp)
                    }
                    Spacer(Modifier.height(36.dp))
                }

                // Vaccination summary
                Section(title = "Lịch tiêm chủng") {
                    if (pet.vaccinations.isEmpty()) {
                        Text(text = "Chưa có lịch tiêm chủng", color = TextSecondary, fontSize = 14.sp)
                    } else {
                        // PET-02: Split count by status so user sees pending/completed separately
                        val pendingCount = pet.vaccinations.count {
                            it.status == VaccinationStatus.PENDING ||
                            it.status == VaccinationStatus.OVERDUE
                        }
                        val scheduledCount = pet.vaccinations.count {
                            it.status == VaccinationStatus.SCHEDULED
                        }
                        val completedCount = pet.vaccinations.count {
                            it.status == VaccinationStatus.COMPLETED
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (pendingCount > 0) VaccinationCountChip("$pendingCount cần tiêm", Color(0xFFFFEBEE), Color(0xFFC62828))
                            if (scheduledCount > 0) VaccinationCountChip("$scheduledCount đã hẹn", Color(0xFFE3EAF8), Color(0xFF1565C0))
                            if (completedCount > 0) VaccinationCountChip("$completedCount đã tiêm", Color(0xFFE8F5E9), Color(0xFF2E7D32))
                        }
                        // Show top 3 by priority: OVERDUE first, then PENDING, then SCHEDULED
                        val prioritized = pet.vaccinations
                            .sortedWith(compareBy {
                                when (it.status) {
                                    VaccinationStatus.OVERDUE   -> 0
                                    VaccinationStatus.PENDING   -> 1
                                    VaccinationStatus.SCHEDULED -> 2
                                    else -> 3
                                }
                            })
                            .take(3)
                        prioritized.forEach { v ->
                            VaccinationCard(vaccination = v, onClick = { onVaccinationClick(v.id) })
                            Spacer(Modifier.height(12.dp))
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    TextButton(
                        onClick = { onVaccinationsViewAll(pet.id, pet.name, pet.type, pet.birthDate) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = when {
                                pet.vaccinations.isEmpty()  -> "Thêm lịch tiêm chủng"
                                pet.vaccinations.size > 3   -> "Xem tất cả (${pet.vaccinations.size}) →"
                                else                         -> "Xem chi tiết →"
                            },
                            color = HealthPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(Modifier.height(48.dp))

                Button(
                    onClick = onEditClick,
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HealthPrimary),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Text("Chỉnh sửa hồ sơ", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }

                Spacer(Modifier.height(12.dp))

                TextButton(onClick = onDeleteClick, modifier = Modifier.fillMaxWidth()) {
                    Text("Xóa hồ sơ thú cưng", color = Color.Red, fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

@Composable
private fun Section(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimary,
            letterSpacing = (-0.3).sp
        )
        Spacer(Modifier.height(16.dp))
        content()
    }
}

@Composable
private fun RowScope.StatBox(label: String, value: String) {
    Surface(
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F5F9))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, fontSize = 12.sp, color = HealthMuted, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
        }
    }
}

@Composable
private fun VaccinationCountChip(label: String, bgColor: Color, textColor: Color) {
    Surface(
        color = bgColor,
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun VaccinationCard(
    vaccination: com.example.vetbook.domain.models.Vaccination,
    onClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F5F9))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            val isCompleted = vaccination.status == VaccinationStatus.COMPLETED
            val isOverdue   = vaccination.status == VaccinationStatus.OVERDUE

            Surface(
                color = when {
                    isCompleted -> Color(0xFFE8F5E9)
                    isOverdue   -> Color(0xFFFFEBEE)
                    else        -> Color(0xFFFFF3E0)
                },
                shape = CircleShape,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = when {
                            isCompleted -> "✓"
                            isOverdue   -> "!"
                            else        -> "○"
                        },
                        color = when {
                            isCompleted -> Color(0xFF2E7D32)
                            isOverdue   -> Color(0xFFC62828)
                            else        -> Color(0xFFEF6C00)
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = vaccination.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)

                val fmt = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
                val dateText = when {
                    vaccination.completedDate != null ->
                        vaccination.completedDate.atZone(ZoneId.systemDefault()).format(fmt)
                    vaccination.scheduledDate != null ->
                        vaccination.scheduledDate.atZone(ZoneId.systemDefault()).format(fmt)
                    else -> "Chưa xếp lịch"
                }
                Text(text = dateText, fontSize = 13.sp, color = HealthMuted)

                vaccination.veterinarianName?.let {
                    Text(text = "👨‍⚕️ $it", fontSize = 12.sp, color = HealthMuted)
                }
            }

            if (isCompleted) {
                Surface(color = Color(0xFFE8F5E9), shape = RoundedCornerShape(8.dp)) {
                    Text(
                        text = "Đã tiêm",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                }
            }
        }
    }
}