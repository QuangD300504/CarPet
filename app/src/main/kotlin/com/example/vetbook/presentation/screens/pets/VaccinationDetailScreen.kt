package com.example.vetbook.presentation.screens.pets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import coil3.compose.AsyncImage
import com.example.vetbook.domain.models.Vaccination
import com.example.vetbook.domain.models.VaccinationStatus
import com.example.vetbook.domain.models.VaccinationType
import com.example.vetbook.presentation.theme.*
import com.example.vetbook.presentation.components.common.SnackbarType
import com.example.vetbook.presentation.components.common.VetBookSnackbar
import com.example.vetbook.presentation.components.common.VetBookSnackbarHost
import com.example.vetbook.presentation.viewmodels.VaccinationDetailViewModel
import com.example.vetbook.utils.compressImageForAvatar
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// ── Colors ────────────────────────────────────────────────────────────────────

private val Teal       = Color(0xFF0D7377)
private val TealLight  = Color(0xFFE6F4F1)
private val TealDark   = Color(0xFF0D5E61)
private val BlueAccent = Color(0xFF1565C0)
private val BlueBg     = Color(0xFFE3EAF8)
private val RedAccent  = Color(0xFFC62828)
private val RedBg      = Color(0xFFFFEBEE)
private val GreenAccent= Color(0xFF2E7D32)
private val GreenBg    = Color(0xFFE8F5E9)
private val GreyAccent = Color(0xFF757575)
private val GreyBg     = Color(0xFFF5F5F5)
private val Surface0   = Color(0xFFF7F8FA)
private val Border     = Color(0xFFECEFF3)
private val TextHint   = Color(0xFF8A9BB0)
private val TextBody   = Color(0xFF0F1923)

private data class StatusConfig(
    val label: String,
    val accent: Color,
    val bg: Color
)

private fun statusConfig(status: VaccinationStatus) = when (status) {
    VaccinationStatus.PENDING    -> StatusConfig("Cần đặt lịch", Teal,       TealLight)
    VaccinationStatus.SCHEDULED  -> StatusConfig("Đã hẹn",       BlueAccent, BlueBg)
    VaccinationStatus.COMPLETED  -> StatusConfig("Đã tiêm",      GreenAccent,GreenBg)
    VaccinationStatus.OVERDUE    -> StatusConfig("Quá hạn",      RedAccent,  RedBg)
    VaccinationStatus.SKIPPED    -> StatusConfig("Bỏ qua",       GreyAccent, GreyBg)
}

// ── Screen ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaccinationDetailScreen(
    vaccinationId: String,
    viewModel: VaccinationDetailViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onVetClick: (String) -> Unit = {},
    onBookAppointment: (vaccinationId: String, doctorId: String, vaccineTitle: String) -> Unit = { _, _, _ -> }
) {
    val vaccination by viewModel.vaccination.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val message by viewModel.message.collectAsState()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showMarkDoneDialog by remember { mutableStateOf(false) }
    var showCertFullscreen by remember { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val bytes = compressImageForAvatar(context, it)
            if (bytes != null) viewModel.uploadCertificate(bytes)
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(message, error) {
        message?.let {
            scope.launch { VetBookSnackbar.show(snackbarHostState, it, SnackbarType.Success) }
            viewModel.clearMessages()
        }
        error?.let {
            scope.launch { VetBookSnackbar.show(snackbarHostState, it, SnackbarType.Error) }
            viewModel.clearMessages()
        }
    }

    // Delete dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.deleteVaccination(onDeleted = onBackClick)
                }) { Text("Xóa", color = RedAccent, fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Hủy", color = TextHint)
                }
            },
            title = { Text("Xóa lịch tiêm?", fontWeight = FontWeight.SemiBold) },
            text = { Text("Thao tác này không thể hoàn tác.", color = TextHint, fontSize = 14.sp) },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Mark done dialog
    if (showMarkDoneDialog) {
        AlertDialog(
            onDismissRequest = { showMarkDoneDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        showMarkDoneDialog = false
                        viewModel.markCompleted()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenAccent),
                    shape = RoundedCornerShape(10.dp)
                ) { Text("Xác nhận đã tiêm", fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                TextButton(onClick = { showMarkDoneDialog = false }) {
                    Text("Chưa", color = TextHint)
                }
            },
            title = { Text("Xác nhận tiêm?", fontWeight = FontWeight.SemiBold) },
            text = {
                Text(
                    "Xác nhận ${vaccination?.title ?: "vaccine"} đã được tiêm. " +
                    "Nếu có lịch nhắc lại, mũi tiếp theo sẽ tự tạo.",
                    fontSize = 14.sp,
                    color = TextHint,
                    lineHeight = 20.sp
                )
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Cert fullscreen
    if (showCertFullscreen && vaccination?.certificateUrl != null) {
        AlertDialog(
            onDismissRequest = { showCertFullscreen = false },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showCertFullscreen = false }) {
                    Text("Đóng", color = Teal)
                }
            },
            title = { Text("Chứng nhận", fontWeight = FontWeight.SemiBold) },
            text = {
                AsyncImage(
                    model = vaccination?.certificateUrl,
                    contentDescription = "Certificate",
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 200.dp, max = 400.dp),
                    contentScale = ContentScale.Fit
                )
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }

    Scaffold(
        snackbarHost = { VetBookSnackbarHost(snackbarHostState) },
        containerColor = Surface0,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Chi tiết tiêm chủng",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 17.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Xóa", tint = RedAccent.copy(alpha = 0.7f))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Box(Modifier.fillMaxSize().padding(paddingValues)) {
            when {
                isLoading && vaccination == null -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Teal,
                        strokeWidth = 2.dp
                    )
                }
                error != null && vaccination == null -> {
                    Column(
                        Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Không thể tải dữ liệu", color = RedAccent, fontSize = 14.sp)
                        Spacer(Modifier.height(8.dp))
                        TextButton(onClick = { viewModel.loadVaccination() }) {
                            Text("Thử lại", color = Teal)
                        }
                    }
                }
                vaccination != null -> {
                    DetailContent(
                        vaccination = vaccination!!,
                        onVetClick = onVetClick,
                        onBookAppointment = onBookAppointment,
                        onMarkDone = { showMarkDoneDialog = true },
                        onMarkSkipped = { viewModel.markSkipped() },
                        onUploadCert = { imagePicker.launch("image/*") },
                        onCertClick = { showCertFullscreen = true }
                    )
                }
            }
        }
    }
}

// ── Content ───────────────────────────────────────────────────────────────────

@Composable
private fun DetailContent(
    vaccination: Vaccination,
    onVetClick: (String) -> Unit,
    onBookAppointment: (vaccinationId: String, doctorId: String, vaccineTitle: String) -> Unit,
    onMarkDone: () -> Unit,
    onMarkSkipped: () -> Unit,
    onUploadCert: () -> Unit,
    onCertClick: () -> Unit
) {
    val sc = statusConfig(vaccination.status)
    val fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        // ── Header card ──────────────────────────────────────────────────────
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = Color.White,
            shadowElevation = 1.dp
        ) {
            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxHeight()
                        .background(sc.accent)
                )
                Column(modifier = Modifier.padding(16.dp)) {
                    // Status pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(sc.bg)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(sc.label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = sc.accent)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        vaccination.title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextBody
                    )
                    vaccination.alsoKnownAs?.let {
                        Text(it, fontSize = 13.sp, color = TextHint)
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TypeChip(vaccination.type)
                        if (vaccination.isRecurring) {
                            Chip("Nhắc lại", Teal.copy(alpha = 0.08f), Teal)
                        }
                    }
                }
            }
        }

        // ── Description ──────────────────────────────────────────────────────
        if (vaccination.description != null || vaccination.lifestyleTrigger != null) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = Color.White,
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SectionLabel("Về vaccine này")
                    Spacer(Modifier.height(6.dp))
                    vaccination.description?.let { desc ->
                        Text(desc, fontSize = 13.sp, color = TextHint, lineHeight = 20.sp)
                    }
                    vaccination.lifestyleTrigger?.let { trigger ->
                        Spacer(Modifier.height(10.dp))
                        Row(
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(BlueAccent.copy(alpha = 0.07f))
                                .padding(10.dp)
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = BlueAccent,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Column {
                                Text(
                                    "Khuyến nghị khi:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = BlueAccent
                                )
                                Text(
                                    trigger,
                                    fontSize = 12.sp,
                                    color = BlueAccent.copy(alpha = 0.8f),
                                    lineHeight = 17.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── Dates ────────────────────────────────────────────────────────────
        val hasDates = vaccination.scheduledDate != null ||
                vaccination.completedDate != null ||
                vaccination.nextDueDate != null

        if (hasDates) {
            InfoCard {
                SectionLabel("Ngày tháng")
                Spacer(Modifier.height(10.dp))
                vaccination.scheduledDate?.let {
                    InfoRow("Ngày hẹn", it.atZone(ZoneId.systemDefault()).format(fmt))
                }
                vaccination.completedDate?.let {
                    InfoRow("Ngày tiêm", it.atZone(ZoneId.systemDefault()).format(fmt), highlight = true)
                }
                vaccination.nextDueDate?.let {
                    InfoRow("Mũi tiếp theo", it.atZone(ZoneId.systemDefault()).format(fmt))
                }
            }
        }

        // ── Vet / clinic ─────────────────────────────────────────────────────
        if (vaccination.veterinarianName != null || vaccination.clinicName != null) {
            InfoCard {
                SectionLabel("Bác sĩ & phòng khám")
                Spacer(Modifier.height(10.dp))
                vaccination.veterinarianName?.let {
                    InfoRow("Bác sĩ", it)
                }
                vaccination.clinicName?.let {
                    InfoRow("Phòng khám", it)
                }
                vaccination.veterinarianId?.let { vetId ->
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { onVetClick(vetId) },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Teal),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Teal.copy(alpha = 0.4f)),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text("Xem hồ sơ bác sĩ", fontSize = 12.sp)
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, null, Modifier.size(13.dp))
                    }
                }
            }
        }

        // ── Vaccine info ─────────────────────────────────────────────────────
        if (vaccination.manufacturer != null || vaccination.batchNumber != null) {
            InfoCard {
                SectionLabel("Thông tin vaccine")
                Spacer(Modifier.height(10.dp))
                vaccination.manufacturer?.let { InfoRow("Hãng sản xuất", it) }
                vaccination.batchNumber?.let { InfoRow("Số lô", it) }
            }
        }

        // ── Notes ────────────────────────────────────────────────────────────
        vaccination.notes?.let { notes ->
            InfoCard {
                SectionLabel("Ghi chú")
                Spacer(Modifier.height(8.dp))
                Text(notes, fontSize = 13.sp, color = TextBody, lineHeight = 20.sp)
            }
        }

        // ── Certificate ──────────────────────────────────────────────────────
        InfoCard {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionLabel("Chứng nhận")
                TextButton(
                    onClick = onUploadCert,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.Upload, null, Modifier.size(14.dp), tint = Teal)
                    Spacer(Modifier.width(3.dp))
                    Text(
                        if (vaccination.certificateUrl != null) "Thay đổi" else "Tải lên",
                        fontSize = 12.sp,
                        color = Teal
                    )
                }
            }
            if (vaccination.certificateUrl != null) {
                Spacer(Modifier.height(8.dp))
                AsyncImage(
                    model = vaccination.certificateUrl,
                    contentDescription = "Certificate",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(onClick = onCertClick),
                    contentScale = ContentScale.Crop
                )
            } else {
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Surface0)
                        .clickable(onClick = onUploadCert),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Chưa có chứng nhận — nhấn để tải lên", fontSize = 12.sp, color = TextHint)
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // ── Primary actions ──────────────────────────────────────────────────
        when (vaccination.status) {
            VaccinationStatus.PENDING -> {
                Button(
                    onClick = { onBookAppointment(vaccination.id, vaccination.veterinarianId ?: "", vaccination.title) },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Teal),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CalendarMonth, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Đặt lịch tiêm", fontWeight = FontWeight.SemiBold)
                }
            }
            VaccinationStatus.SCHEDULED, VaccinationStatus.OVERDUE -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onMarkDone,
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenAccent),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Check, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Đã tiêm", fontWeight = FontWeight.SemiBold)
                    }
                    OutlinedButton(
                        onClick = onMarkSkipped,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextHint),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Border)
                    ) {
                        Text("Bỏ qua", fontWeight = FontWeight.Medium)
                    }
                }
                // VAC-03: Re-book button for when appointment was missed or cancelled
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { onBookAppointment(vaccination.id, vaccination.veterinarianId ?: "", vaccination.title) },
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Teal),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Teal.copy(alpha = 0.5f))
                ) {
                    Icon(Icons.Default.CalendarMonth, null, Modifier.size(15.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        if (vaccination.status == VaccinationStatus.OVERDUE) "Đặt lại lịch" else "Đổi lịch hẹn",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            else -> {}
        }

        Spacer(Modifier.height(32.dp))
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

@Composable
private fun InfoCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextHint, letterSpacing = 0.4.sp)
}

@Composable
private fun InfoRow(label: String, value: String, highlight: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = TextHint)
        Text(
            value,
            fontSize = 13.sp,
            fontWeight = if (highlight) FontWeight.SemiBold else FontWeight.Normal,
            color = if (highlight) GreenAccent else TextBody
        )
    }
}

@Composable
private fun TypeChip(type: VaccinationType) {
    val (label, color) = when (type) {
        VaccinationType.CORE            -> "Core" to Teal
        VaccinationType.REGIONAL        -> "Regional" to Color(0xFFB07A00)
        VaccinationType.LIFESTYLE       -> "Lifestyle" to BlueAccent
        VaccinationType.NOT_RECOMMENDED -> "Not rec." to RedAccent
        VaccinationType.CUSTOM          -> "Custom" to GreyAccent
    }
    Chip(label, color.copy(alpha = 0.08f), color)
}

@Composable
private fun Chip(label: String, bg: Color, textColor: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = textColor)
    }
}