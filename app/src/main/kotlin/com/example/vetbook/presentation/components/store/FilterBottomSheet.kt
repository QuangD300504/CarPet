package com.example.vetbook.presentation.components.store

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vetbook.presentation.theme.HealthPrimary
import com.example.vetbook.presentation.theme.HealthMuted
import com.example.vetbook.presentation.theme.TextPrimary
import com.example.vetbook.presentation.viewmodels.SortOption
import com.example.vetbook.utils.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    currentSort: SortOption,
    currentPriceMin: Float,
    currentPriceMax: Float,
    currentInStockOnly: Boolean,
    onSortChange: (SortOption) -> Unit,
    onPriceRangeChange: (Float, Float) -> Unit,
    onInStockChange: (Boolean) -> Unit,
    onReset: () -> Unit,
    onApply: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState()
) {
    ModalBottomSheet(
        onDismissRequest = onApply,
        sheetState = sheetState,
        containerColor = androidx.compose.ui.graphics.Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .padding(bottom = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Bộ lọc & Sắp xếp",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                TextButton(onClick = onReset) {
                    Text("Đặt lại", color = HealthPrimary, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(24.dp))

            // Sort section
            Text(
                text = "Sắp xếp theo",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(Modifier.height(12.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(SortOption.entries) { option ->
                    FilterChip(
                        selected = option == currentSort,
                        onClick = { onSortChange(option) },
                        label = {
                            Text(
                                text = option.label,
                                fontSize = 13.sp,
                                fontWeight = if (option == currentSort) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = HealthPrimary,
                            selectedLabelColor = androidx.compose.ui.graphics.Color.White
                        )
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Price range section
            Text(
                text = "Khoảng giá",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${CurrencyFormatter.format(currentPriceMin.toDouble())} — ${CurrencyFormatter.format(currentPriceMax.toDouble())}",
                fontSize = 13.sp,
                color = HealthMuted
            )
            Spacer(Modifier.height(8.dp))

            var priceMin by remember(currentPriceMin) { mutableStateOf(currentPriceMin) }
            var priceMax by remember(currentPriceMax) { mutableStateOf(currentPriceMax) }

            RangeSlider(
                value = priceMin..priceMax,
                onValueChange = { range ->
                    priceMin = range.start
                    priceMax = range.endInclusive
                },
                onValueChangeFinished = {
                    onPriceRangeChange(priceMin, priceMax)
                },
                valueRange = 0f..5_000_000f,
                steps = 49
            )

            Spacer(Modifier.height(16.dp))

            // In-stock toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Chỉ hiển thị còn hàng",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Ẩn các sản phẩm đã hết",
                        fontSize = 12.sp,
                        color = HealthMuted
                    )
                }
                Switch(
                    checked = currentInStockOnly,
                    onCheckedChange = onInStockChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = androidx.compose.ui.graphics.Color.White,
                        checkedTrackColor = HealthPrimary
                    )
                )
            }

            Spacer(Modifier.height(28.dp))

            Button(
                onClick = onApply,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = HealthPrimary),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Áp dụng",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}
