package com.example.vetbook.presentation.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vetbook.presentation.theme.HealthMuted
import com.example.vetbook.presentation.theme.HealthPrimary
import com.example.vetbook.presentation.theme.HealthSurface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(onBackClick: () -> Unit = {}) {

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Chính sách bảo mật", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
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

            // Effective date
            Surface(color = HealthSurface, shape = RoundedCornerShape(12.dp)) {
                Text(
                    text = "Có hiệu lực từ: 01/01/2025 · Phiên bản: 1.0",
                    fontSize = 13.sp,
                    color = HealthPrimary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }

            PolicySection(
                number = "1",
                title = "Thông tin chúng tôi thu thập",
                body = """
                    VetBook thu thập các loại thông tin sau khi bạn sử dụng ứng dụng:
                    
                    • Thông tin tài khoản: tên, địa chỉ email, số điện thoại khi bạn đăng ký.
                    • Thông tin thú cưng: tên, loài, giống, cân nặng, ngày sinh, ảnh và lịch tiêm chủng mà bạn tự nhập.
                    • Thông tin lịch hẹn: bác sĩ thú y, ngày giờ, phương thức thanh toán và trạng thái thanh toán.
                    • Dữ liệu thiết bị: mã FCM token để gửi thông báo nhắc lịch.
                    • Ảnh do bạn tải lên (ảnh đại diện, ảnh thú cưng, chứng nhận tiêm chủng) được lưu trữ trên Cloudinary.
                """.trimIndent()
            )

            PolicySection(
                number = "2",
                title = "Mục đích sử dụng thông tin",
                body = """
                    Thông tin thu thập được sử dụng để:
                    
                    • Cung cấp và vận hành các tính năng của VetBook (đặt lịch, theo dõi tiêm chủng, mua sắm).
                    • Gửi thông báo nhắc lịch tiêm và lịch khám qua Firebase Cloud Messaging.
                    • Xử lý thanh toán qua PayOS (chúng tôi không lưu trữ thông tin thẻ ngân hàng).
                    • Cải thiện trải nghiệm người dùng và khắc phục lỗi ứng dụng.
                    • Tuân thủ các yêu cầu pháp lý khi cần thiết.
                """.trimIndent()
            )

            PolicySection(
                number = "3",
                title = "Chia sẻ thông tin với bên thứ ba",
                body = """
                    VetBook không bán dữ liệu cá nhân của bạn. Chúng tôi chỉ chia sẻ với các nhà cung cấp dịch vụ tin cậy:
                    
                    • Firebase (Google): lưu trữ dữ liệu, xác thực và thông báo đẩy.
                    • Cloudinary: lưu trữ và phân phối ảnh.
                    • PayOS: xử lý thanh toán.
                    • Bác sĩ thú y: tên thú cưng và ghi chú bạn cung cấp khi đặt lịch.
                    
                    Tất cả nhà cung cấp đều bị ràng buộc bởi các thỏa thuận bảo mật dữ liệu.
                """.trimIndent()
            )

            PolicySection(
                number = "4",
                title = "Lưu trữ và bảo mật dữ liệu",
                body = """
                    • Dữ liệu được lưu trữ trên Firebase Firestore với mã hóa khi truyền (TLS) và khi nghỉ.
                    • Bạn có thể xóa tài khoản bất cứ lúc nào từ Hồ sơ → Bảo mật → Xóa tài khoản.
                    • Khi tài khoản bị xóa, dữ liệu Firestore liên quan sẽ được xóa theo chính sách lưu giữ của Firebase.
                    • Ảnh trên Cloudinary có thể còn tồn tại tối đa 30 ngày sau khi xóa tài khoản.
                """.trimIndent()
            )

            PolicySection(
                number = "5",
                title = "Quyền của bạn",
                body = """
                    Bạn có quyền:
                    
                    • Truy cập và chỉnh sửa thông tin cá nhân từ màn hình Chỉnh sửa hồ sơ.
                    • Xóa dữ liệu thú cưng, lịch tiêm chủng từ ứng dụng bất kỳ lúc nào.
                    • Yêu cầu xuất toàn bộ dữ liệu của bạn bằng cách liên hệ với chúng tôi.
                    • Xóa tài khoản hoàn toàn từ phần Bảo mật trong ứng dụng.
                    • Tắt thông báo đẩy từ cài đặt thiết bị hoặc phần Hồ sơ → Thông báo.
                """.trimIndent()
            )

            PolicySection(
                number = "6",
                title = "Trẻ em",
                body = """
                    VetBook không được thiết kế cho trẻ em dưới 13 tuổi. Nếu bạn là phụ huynh và phát hiện con bạn đã cung cấp thông tin cá nhân, vui lòng liên hệ để chúng tôi xóa dữ liệu đó.
                """.trimIndent()
            )

            PolicySection(
                number = "7",
                title = "Liên hệ",
                body = """
                    Nếu bạn có câu hỏi về chính sách này hoặc cần hỗ trợ liên quan đến dữ liệu cá nhân, vui lòng liên hệ:
                    
                    Email: support@vetbook.app
                    
                    Chúng tôi sẽ phản hồi trong vòng 5 ngày làm việc.
                """.trimIndent()
            )

            // Footer
            Text(
                text = "Chính sách này có thể được cập nhật định kỳ. Thay đổi lớn sẽ được thông báo trong ứng dụng.",
                fontSize = 12.sp,
                color = HealthMuted,
                lineHeight = 18.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    }
}

@Composable
private fun PolicySection(number: String, title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Surface(
                color = HealthPrimary,
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.size(24.dp)
            ) {
                Box(contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Text(number, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.width(10.dp))
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E))
        }
        Text(
            text = body,
            fontSize = 14.sp,
            color = Color(0xFF4B5563),
            lineHeight = 22.sp
        )
    }
}