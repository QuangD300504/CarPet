package com.example.vetbook.presentation.components.pets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.heightIn
import com.example.vetbook.domain.models.Vaccination
import com.example.vetbook.domain.models.VaccinationStatus
import com.example.vetbook.domain.models.VaccinationType
import com.example.vetbook.presentation.theme.*
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val TYPE_CONFIG = mapOf(
    VaccinationType.CORE to TypeStyle(
        "Cốt lõi (Core)",
        Color(0xFFDCFCE7),
        Color(0xFF166534),
        defaultChecked = true,
        locked = true
    ),
    VaccinationType.REGIONAL to TypeStyle(
        "Vùng (Regional)",
        Color(0xFFFEF9C3),
        Color(0xFF854D0E),
        defaultChecked = true,
        locked = false
    ),
    VaccinationType.LIFESTYLE to TypeStyle(
        "Lối sống (Lifestyle)",
        Color(0xFFDBEAFE),
        Color(0xFF1E40AF),
        defaultChecked = false,
        locked = false
    ),
    VaccinationType.NOT_RECOMMENDED to TypeStyle(
        "Không khuyến khích",
        Color(0xFFFEE2E2),
        Color(0xFF991B1B),
        defaultChecked = false,
        locked = true
    ),
    VaccinationType.CUSTOM to TypeStyle(
        "Tùy chỉnh",
        Color(0xFFF3F4F6),
        Color(0xFF374151),
        defaultChecked = true,
        locked = false
    )
)

private data class TypeStyle(
    val label: String,
    val bgColor: Color,
    val textColor: Color,
    val defaultChecked: Boolean,
    val locked: Boolean
)

private val TYPE_TOOLTIPS = mapOf(
    VaccinationType.REGIONAL to "Được khuyến nghị ở hầu hết các vùng — kiểm tra với bác sĩ thú y",
    VaccinationType.LIFESTYLE to "Được khuyến nghị dựa trên hoạt động của thú cưng",
    VaccinationType.NOT_RECOMMENDED to "WSAVA khuyên không nên sử dụng — bằng chứng lâm sàng không đủ"
)

@Composable
fun VaccineReviewModal(
    generatedRecords: List<Vaccination>,
    onConfirm: (List<Vaccination>) -> Unit,
    onClose: () -> Unit
) {
    var selectedIds by rememberSaveable {
        mutableStateOf(
            generatedRecords
                .filter { TYPE_CONFIG[it.type]?.defaultChecked == true }
                .map { it.id }
                .toSet()
        )
    }

    val grouped = generatedRecords.groupBy { it.type }
    val typeOrder = listOf(
        VaccinationType.CORE,
        VaccinationType.REGIONAL,
        VaccinationType.LIFESTYLE,
        VaccinationType.CUSTOM
    ).filter { grouped.containsKey(it) }

    val selectedCount = selectedIds.size
    val totalCount = generatedRecords.size

    AlertDialog(
        onDismissRequest = onClose,
        confirmButton = {
            Button(
                onClick = {
                    val selected = generatedRecords.filter { it.id in selectedIds }
                    onConfirm(selected)
                },
                enabled = selectedIds.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = HealthPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Xác nhận ($selectedCount/$totalCount)", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onClose) {
                Text("Bỏ qua", color = TextSecondary, fontWeight = FontWeight.Bold)
            }
        },
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Xem lại lịch tiêm",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Bật/tắt các mũi tiêm bạn muốn thêm",
                        fontSize = 13.sp,
                        color = HealthMuted
                    )
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Đóng", tint = HealthMuted)
                }
            }
        },
        text = {
            Column (
        modifier = Modifier
            .heightIn(max = 420.dp)
            .verticalScroll(rememberScrollState())
    ) {
                typeOrder.forEach { type ->
                    val records = grouped[type] ?: return@forEach
                    val style = TYPE_CONFIG[type] ?: return@forEach
                    val tooltip = TYPE_TOOLTIPS[type]

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = if (type == typeOrder.first()) 0.dp else 12.dp),
                        color = style.bgColor,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = style.label,
                                    fontWeight = FontWeight.Bold,
                                    color = style.textColor,
                                    fontSize = 14.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                if (!style.locked) {
                                    Text(
                                        text = "${records.count { it.id in selectedIds }}/${records.size}",
                                        fontSize = 12.sp,
                                        color = style.textColor.copy(alpha = 0.7f),
                                        fontWeight = FontWeight.Medium
                                    )
                                } else {
                                    Surface(
                                        color = Success.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "Bắt buộc",
                                            fontSize = 11.sp,
                                            color = Success,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            if (tooltip != null) {
                                Spacer(Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.Top) {
                                    Icon(
                                        Icons.Default.Info,
                                        contentDescription = null,
                                        tint = style.textColor.copy(alpha = 0.6f),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = tooltip,
                                        fontSize = 11.sp,
                                        color = style.textColor.copy(alpha = 0.7f),
                                        lineHeight = 14.sp
                                    )
                                }
                            }
                        }
                    }

                    records.forEach { record ->
                        val isSelected = record.id in selectedIds
                        val isLocked = style.locked
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .then(
                                    if (!isLocked) {
                                        Modifier.clickable {
                                            selectedIds = if (isSelected) selectedIds - record.id
                                            else selectedIds + record.id
                                        }
                                    } else Modifier
                                )
                                .background(
                                    if (isSelected) style.bgColor.copy(alpha = 0.5f)
                                    else Color.Transparent
                                )
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isLocked) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = style.textColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            } else {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { checked ->
                                        selectedIds = if (checked) selectedIds + record.id
                                        else selectedIds - record.id
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = style.textColor,
                                        uncheckedColor = HealthMuted
                                    )
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = record.title,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp,
                                    color = TextPrimary
                                )
                                record.alsoKnownAs?.let { aka ->
    Text(text = aka, fontSize = 12.sp, color = HealthMuted)
}
record.description?.let { desc ->
    Spacer(Modifier.height(2.dp))
    Text(text = desc, fontSize = 11.sp, color = HealthMuted, lineHeight = 14.sp)
}
                            }
                            Spacer(Modifier.width(8.dp))
                            Column(horizontalAlignment = Alignment.End) {
                                if (record.isRecurring) {
                                    Surface(
                                        color = HealthSurface,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "🔄",
                                            fontSize = 10.sp,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp)
    )
}
