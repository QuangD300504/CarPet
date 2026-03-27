package com.example.vetbook.presentation.screens.store

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vetbook.presentation.components.topbars.SimpleTopBar
import com.example.vetbook.presentation.theme.HealthPrimary
import com.example.vetbook.presentation.theme.HealthSurface
import com.example.vetbook.presentation.theme.HealthMuted
import com.example.vetbook.presentation.theme.TextPrimary
import com.example.vetbook.presentation.theme.Success
import com.example.vetbook.presentation.theme.Error as ErrorColor

/**
 * @param isSuccess         Whether the payment/booking succeeded.
 * @param onContinueShoppingClick  Primary action for the store flow.
 * @param onHomeClick       Back / home navigation.
 * @param onViewCalendarClick  If non-null, a "View Calendar" button is shown on success
 *                          (used by the vet-booking flow).
 * @param onTryAgainClick   If non-null, a "Try Again" button is shown on failure
 *                          (used by the vet-booking flow).
 */
@Composable
fun PaymentResultScreen(
    isSuccess: Boolean,
    onContinueShoppingClick: () -> Unit,
    onHomeClick: () -> Unit,
    onViewCalendarClick: (() -> Unit)? = null,
    onTryAgainClick: (() -> Unit)? = null,
    onViewVaccinationClick: (() -> Unit)? = null,
    onOrderHistoryClick: (() -> Unit)? = null
) {
    // Vet-booking mode: we have a calendar action (success) or try-again action (failure)
    val isVetBookingFlow = onViewCalendarClick != null || onTryAgainClick != null

    val titleText = when {
        isSuccess && isVetBookingFlow  -> "Đặt lịch thành công!"
        isSuccess                       -> "Thanh toán thành công!"
        !isSuccess && isVetBookingFlow  -> "Đặt lịch chưa hoàn tất"
        else                            -> "Thanh toán thất bại"
    }
    val subtitleText = when {
        isSuccess && isVetBookingFlow  ->
            "Lịch hẹn của bạn đã được xác nhận.\nBạn có thể xem trong lịch của mình."
        isSuccess                       ->
            "Giao dịch của bạn đã được hoàn tất thành công.\nCảm ơn bạn đã tin dùng!"
        !isSuccess && isVetBookingFlow  ->
            "Thanh toán bị hủy hoặc không thành công.\nLịch hẹn chưa được giữ cho bạn."
        else                            ->
            "Giao dịch đã bị hủy hoặc bị từ chối.\nVui lòng thử lại sau."
    }

    Scaffold(
        topBar = {
            SimpleTopBar(
                title = if (isVetBookingFlow) "Xác nhận đặt lịch" else "Trạng thái thanh toán",
                onBackClick = onHomeClick
            )
        },
        containerColor = Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        color = if (isSuccess) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isSuccess) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = if (isSuccess) "Thành công" else "Thất bại",
                    tint = if (isSuccess) Success else ErrorColor,
                    modifier = Modifier.size(64.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = titleText,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = subtitleText,
                fontSize = 16.sp,
                color = HealthMuted,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Primary action
            if (isSuccess && onViewCalendarClick != null) {
                // Vet booking success → "View Calendar"
                Button(
                    onClick = onViewCalendarClick,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HealthPrimary),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Xem lịch hẹn",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            } else if (!isSuccess && onTryAgainClick != null) {
                // Vet booking failure → "Try Again"
                Button(
                    onClick = onTryAgainClick,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HealthPrimary),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Thử lại",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            } else {
                // Store payment flow — keep original button
                Button(
                    onClick = onContinueShoppingClick,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HealthPrimary),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Tiếp tục mua sắm",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Show order history shortcut for store payment success
            if (isSuccess && onOrderHistoryClick != null && onViewCalendarClick == null) {
                OutlinedButton(
                    onClick = onOrderHistoryClick,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, HealthPrimary.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = HealthPrimary)
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingBag,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Xem đơn hàng",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // VAC-02: Show vaccine shortcut when booking came from vaccine flow
            if (isSuccess && onViewVaccinationClick != null) {
                OutlinedButton(
                    onClick = onViewVaccinationClick,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, HealthPrimary.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = HealthPrimary)
                ) {
                    Icon(
                        imageVector = Icons.Default.Vaccines,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Xem lịch tiêm chủng",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            OutlinedButton(
                onClick = onHomeClick,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
            ) {
                Text(
                    text = "Quay về Trang chủ",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        }
    }
}