package com.example.vetbook.presentation.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vetbook.presentation.theme.HealthMuted
import com.example.vetbook.presentation.theme.HealthPrimary
import com.example.vetbook.presentation.theme.HealthSurface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpSupportScreen(onBackClick: () -> Unit = {}) {

    var expandedFaq by remember { mutableStateOf<Int?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Trợ giúp & Hỗ trợ", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            // ── Contact channels ──────────────────────────────────────────────
            HelpSectionHeader("Liên hệ với chúng tôi")

            ContactCard(
                icon = Icons.Default.Email,
                title = "Email hỗ trợ",
                subtitle = "support@vetbook.app\nPhản hồi trong vòng 1–2 ngày làm việc",
                iconColor = HealthPrimary,
                iconBackground = HealthSurface
            )

            ContactCard(
                icon = Icons.AutoMirrored.Filled.Chat,
                title = "Chat trực tiếp",
                subtitle = "Thứ Hai – Thứ Sáu, 8:00 – 17:00\nThứ Bảy, 8:00 – 12:00",
                iconColor = Color(0xFF0EA5E9),
                iconBackground = Color(0xFFE0F2FE)
            )

            ContactCard(
                icon = Icons.Default.Phone,
                title = "Đường dây hỗ trợ",
                subtitle = "077-2604-956 (cước phí thông thường)\nThứ Hai – Thứ Sáu, 8:00 – 17:00",
                iconColor = Color(0xFF10B981),
                iconBackground = Color(0xFFD1FAE5)
            )

            // ── FAQ ───────────────────────────────────────────────────────────
            HelpSectionHeader("Câu hỏi thường gặp")

            val faqs = listOf(
                "Làm sao để đặt lịch hẹn với bác sĩ thú y?" to
                    "Vào tab Lịch hẹn → chọn bác sĩ → chọn ngày và khung giờ phù hợp → xác nhận và thanh toán. Bạn sẽ nhận thông báo nhắc lịch tự động trước 24 giờ.",
                "Tôi quên mật khẩu, phải làm thế nào?" to
                    "Tại màn hình đăng nhập, nhấn \"Quên mật khẩu\" và nhập email của bạn. Chúng tôi sẽ gửi link đặt lại mật khẩu trong vài phút. Kiểm tra cả hộp thư Spam nếu không thấy.",
                "Làm sao để thêm thú cưng mới?" to
                    "Vào tab Thú cưng → nhấn nút + ở góc trên bên phải → điền đầy đủ thông tin (tên, loài, giống, ngày sinh) → nhấn Lưu. Bạn có thể thêm ảnh cho thú cưng bất cứ lúc nào.",
                "Tôi có thể hủy lịch hẹn không?" to
                    "Có. Vào tab Lịch hẹn → chọn lịch hẹn cần hủy → nhấn Hủy lịch. Chính sách hoàn tiền áp dụng nếu hủy trước 24 giờ. Vui lòng liên hệ hỗ trợ nếu cần hoàn tiền.",
                "Thông báo nhắc lịch không hoạt động?" to
                    "Kiểm tra Cài đặt → Ứng dụng → CarPet → Thông báo và đảm bảo đã bật cho phép. Vào Hồ sơ → Thông báo trong ứng dụng để kiểm tra cài đặt thêm.",
                "Làm sao để xóa hoặc chỉnh sửa lịch tiêm chủng?" to
                    "Vào Thú cưng → chọn thú cưng → Lịch sử tiêm chủng → nhấn vào bản ghi cần chỉnh sửa. Bạn có thể cập nhật thông tin hoặc xóa bản ghi từ màn hình chi tiết."
            )

            faqs.forEachIndexed { index, (question, answer) ->
                FaqItem(
                    question = question,
                    answer = answer,
                    isExpanded = expandedFaq == index,
                    onToggle = { expandedFaq = if (expandedFaq == index) null else index }
                )
            }

            // ── App info ──────────────────────────────────────────────────────
            HelpSectionHeader("Thông tin ứng dụng")

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    AppInfoRow(label = "Phiên bản ứng dụng", value = "1.0.0")
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    AppInfoRow(label = "Hệ điều hành yêu cầu", value = "Android 8.0+")
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    AppInfoRow(label = "Nhà phát triển", value = "CarPet Team")
                }
            }

            // Footer note
            Text(
                text = "Chúng tôi luôn cố gắng phản hồi sớm nhất có thể. Cảm ơn bạn đã sử dụng CarPet!",
                fontSize = 12.sp,
                color = HealthMuted,
                lineHeight = 18.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    }
}

@Composable
private fun HelpSectionHeader(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = HealthMuted,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
    )
}

@Composable
private fun ContactCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconColor: Color,
    iconBackground: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = iconBackground,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1A1A2E))
                Text(subtitle, fontSize = 12.sp, color = HealthMuted, lineHeight = 18.sp)
            }
        }
    }
}

@Composable
private fun FaqItem(
    question: String,
    answer: String,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = if (isExpanded) BorderStroke(1.dp, HealthPrimary.copy(alpha = 0.3f)) else null,
        shadowElevation = 1.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = question,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color(0xFF1A1A2E),
                    modifier = Modifier.weight(1f),
                    lineHeight = 20.sp
                )
                Spacer(Modifier.width(8.dp))
                IconButton(
                    onClick = onToggle,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = HealthPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            if (isExpanded) {
                HorizontalDivider(
                    color = Color(0xFFF1F5F9),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Text(
                    text = answer,
                    fontSize = 13.sp,
                    color = Color(0xFF4B5563),
                    lineHeight = 21.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun AppInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 14.sp, color = HealthMuted)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1A1A2E))
    }
}