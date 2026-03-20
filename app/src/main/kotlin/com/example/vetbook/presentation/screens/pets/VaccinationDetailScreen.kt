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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Upload
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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import coil3.compose.AsyncImage
import com.example.vetbook.domain.models.Vaccination
import com.example.vetbook.domain.models.VaccinationStatus
import com.example.vetbook.domain.models.VaccinationType
import com.example.vetbook.presentation.theme.*
import com.example.vetbook.presentation.viewmodels.VaccinationDetailViewModel
import com.example.vetbook.utils.compressImageForAvatar
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaccinationDetailScreen(
    vaccinationId: String,
    viewModel: VaccinationDetailViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onVetClick: (String) -> Unit = {},
    onBookAppointment: (String) -> Unit = {}
) {
    val vaccination by viewModel.vaccination.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val message by viewModel.message.collectAsState()

    val context = LocalContext.current

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showCertFullscreen by remember { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val bytes = compressImageForAvatar(context, it)
            if (bytes != null) {
                viewModel.uploadCertificate(bytes)
            }
        }
    }

    // Snackbar for messages
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(message, error) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
        error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteVaccination(onDeleted = onBackClick)
                    }
                ) {
                    Text("Xóa", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Hủy", color = TextSecondary)
                }
            },
            title = { Text("Xác nhận xóa", fontWeight = FontWeight.Bold) },
            text = { Text("Bạn có chắc chắn muốn xóa lịch tiêm chủng này không? Thao tác không thể hoàn tác.") },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp)
        )
    }

    // Fullscreen certificate dialog
    if (showCertFullscreen && vaccination?.certificateUrl != null) {
        AlertDialog(
            onDismissRequest = { showCertFullscreen = false },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showCertFullscreen = false }) {
                    Text("Đóng", color = HealthPrimary)
                }
            },
            title = { Text("Chứng nhận tiêm chủng", fontWeight = FontWeight.Bold) },
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
            shape = RoundedCornerShape(24.dp)
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết tiêm chủng", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading && vaccination == null -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = HealthPrimary
                    )
                }
                error != null && vaccination == null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Không thể tải dữ liệu", color = Color.Red)
                        Spacer(Modifier.height(8.dp))
                        TextButton(onClick = { viewModel.loadVaccination() }) {
                            Text("Thử lại", color = HealthPrimary)
                        }
                    }
                }
                vaccination != null -> {
                    VaccinationDetailContent(
                        vaccination = vaccination!!,
                        onVetClick = onVetClick,
                        onBookAppointment = onBookAppointment,
                        onMarkCompleted = { viewModel.markCompleted() },
                        onMarkSkipped = { viewModel.markSkipped() },
                        onDeleteClick = { showDeleteDialog = true },
                        onUploadCert = { imagePicker.launch("image/*") },
                        onCertClick = { showCertFullscreen = true }
                    )
                }
            }
        }
    }
}

@Composable
private fun VaccinationDetailContent(
    vaccination: Vaccination,
    onVetClick: (String) -> Unit,
    onBookAppointment: (String) -> Unit,
    onMarkCompleted: () -> Unit,
    onMarkSkipped: () -> Unit,
    onDeleteClick: () -> Unit,
    onUploadCert: () -> Unit,
    onCertClick: () -> Unit
) {
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    val statusColor = when (vaccination.status) {
        VaccinationStatus.COMPLETED -> Color(0xFF2E7D32)
        VaccinationStatus.OVERDUE -> Color(0xFFC62828)
        VaccinationStatus.SCHEDULED -> Color(0xFFEF6C00)
        VaccinationStatus.SKIPPED -> Color(0xFF757575)
    }
    val statusBgColor = when (vaccination.status) {
        VaccinationStatus.COMPLETED -> Color(0xFFE8F5E9)
        VaccinationStatus.OVERDUE -> Color(0xFFFFEBEE)
        VaccinationStatus.SCHEDULED -> Color(0xFFFFF3E0)
        VaccinationStatus.SKIPPED -> Color(0xFFF5F5F5)
    }
    val statusIcon = when (vaccination.status) {
        VaccinationStatus.COMPLETED -> "✓"
        VaccinationStatus.OVERDUE -> "!"
        VaccinationStatus.SCHEDULED -> "○"
        VaccinationStatus.SKIPPED -> "⊘"
    }
    val statusText = when (vaccination.status) {
        VaccinationStatus.COMPLETED -> "Đã tiêm hoàn thành"
        VaccinationStatus.OVERDUE -> "Đã quá hạn"
        VaccinationStatus.SCHEDULED -> "Sắp tới"
        VaccinationStatus.SKIPPED -> "Đã bỏ qua"
    }

    val typeLabel = when (vaccination.type) {
        VaccinationType.CORE -> "Cốt lõi (Core)"
        VaccinationType.NON_CORE -> "Khuyến nghị (Non-Core)"
        VaccinationType.OPTIONAL -> "Tùy chọn (Optional)"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ===== STATUS HEADER =====
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = statusBgColor
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = CircleShape,
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            statusIcon,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        statusText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                    Text(
                        vaccination.title,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                    Spacer(Modifier.height(4.dp))
                    Surface(
                        color = HealthSurface,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            typeLabel,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = HealthPrimary
                        )
                    }
                }
            }
        }

        // ===== DATES SECTION =====
        DetailSection(title = "📅 Ngày tháng") {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                vaccination.scheduledDate?.let { date ->
                    DateRow(
                        label = "Ngày hẹn",
                        value = date.atZone(ZoneId.systemDefault()).format(dateFormatter)
                    )
                }
                vaccination.completedDate?.let { date ->
                    DateRow(
                        label = "Ngày tiêm",
                        value = date.atZone(ZoneId.systemDefault()).format(dateFormatter),
                        highlight = true
                    )
                }
                vaccination.nextDueDate?.let { date ->
                    DateRow(
                        label = "Mũi tiếp theo",
                        value = date.atZone(ZoneId.systemDefault()).format(dateFormatter)
                    )
                }
            }
        }

        // ===== VET SECTION =====
        if (vaccination.veterinarianName != null || vaccination.clinicName != null) {
            DetailSection(title = "🏥 Thông tin bác sĩ") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    vaccination.veterinarianId?.let { vetId ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onVetClick(vetId) },
                            shape = RoundedCornerShape(16.dp),
                            color = HealthSurface.copy(alpha = 0.5f)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = HealthPrimary,
                                    shape = CircleShape,
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            "👨‍⚕️",
                                            fontSize = 20.sp
                                        )
                                    }
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        vaccination.veterinarianName ?: "",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = TextPrimary
                                    )
                                    vaccination.clinicName?.let {
                                        Text(
                                            it,
                                            fontSize = 13.sp,
                                            color = HealthMuted
                                        )
                                    }
                                }
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Xem",
                                    tint = HealthMuted
                                )
                            }
                        }

                        // Book appointment CTA
                        if (vaccination.status != VaccinationStatus.COMPLETED &&
                            vaccination.status != VaccinationStatus.SKIPPED) {
                            Button(
                                onClick = { onBookAppointment(vaccination.veterinarianId!!) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = HealthPrimary
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("📅 Đặt lịch khám với bác sĩ này", fontWeight = FontWeight.Bold)
                            }
                        }
                    } ?: run {
                        vaccination.clinicName?.let { clinic ->
                            DateRow(label = "Phòng khám", value = clinic)
                        }
                    }
                }
            }
        }

        // ===== VACCINE DETAILS SECTION =====
        if (vaccination.manufacturer != null || vaccination.batchNumber != null) {
            DetailSection(title = "💊 Thông tin vaccine") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    vaccination.manufacturer?.let {
                        DateRow(label = "Hãng sản xuất", value = it)
                    }
                    vaccination.batchNumber?.let {
                        DateRow(label = "Số lô", value = it)
                    }
                }
            }
        }

        // ===== CERTIFICATE SECTION =====
        DetailSection(title = "📄 Chứng nhận") {
            if (vaccination.certificateUrl != null) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    AsyncImage(
                        model = vaccination.certificateUrl,
                        contentDescription = "Certificate",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable(onClick = onCertClick),
                        contentScale = ContentScale.Crop
                    )
                    TextButton(
                        onClick = onUploadCert,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Upload, contentDescription = null, tint = HealthPrimary)
                        Spacer(Modifier.width(4.dp))
                        Text("Thay đổi chứng nhận", color = HealthPrimary)
                    }
                }
            } else {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onUploadCert),
                    shape = RoundedCornerShape(16.dp),
                    color = HealthSurface.copy(alpha = 0.5f)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Chưa có chứng nhận", color = HealthMuted)
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = onUploadCert,
                            colors = ButtonDefaults.buttonColors(containerColor = HealthPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Upload, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("Tải lên chứng nhận")
                        }
                    }
                }
            }
        }

        // ===== NOTES SECTION =====
        vaccination.notes?.let { notes ->
            DetailSection(title = "📝 Ghi chú") {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = HealthSurface.copy(alpha = 0.5f)
                ) {
                    Text(
                        notes,
                        modifier = Modifier.padding(16.dp),
                        fontSize = 14.sp,
                        color = TextPrimary,
                        lineHeight = 22.sp
                    )
                }
            }
        }

        // ===== ACTION BUTTONS =====
        if (vaccination.status == VaccinationStatus.SCHEDULED ||
            vaccination.status == VaccinationStatus.OVERDUE) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onMarkCompleted,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2E7D32)
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Đã tiêm", fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = onMarkSkipped,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = HealthMuted)
                    Spacer(Modifier.width(4.dp))
                    Text("Bỏ qua", color = TextSecondary, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Delete button
        TextButton(
            onClick = onDeleteClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
            Spacer(Modifier.width(4.dp))
            Text("Xóa lịch tiêm chủng", color = Color.Red, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun DetailSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            title,
            fontSize = 17.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimary
        )
        Spacer(Modifier.height(12.dp))
        content()
    }
}

@Composable
private fun DateRow(
    label: String,
    value: String,
    highlight: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 14.sp, color = HealthMuted)
        Text(
            value,
            fontSize = 14.sp,
            fontWeight = if (highlight) FontWeight.Bold else FontWeight.Medium,
            color = if (highlight) Color(0xFF2E7D32) else TextPrimary
        )
    }
}
