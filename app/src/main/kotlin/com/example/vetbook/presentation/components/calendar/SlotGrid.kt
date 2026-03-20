package com.example.vetbook.presentation.components.calendar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vetbook.presentation.theme.HealthMuted
import com.example.vetbook.presentation.theme.HealthPrimary
import com.example.vetbook.presentation.theme.HealthSurface
import java.time.LocalTime

data class SlotOption(
    val label: String,
    val time: LocalTime
) {
    companion object {
        val defaults = listOf(
            SlotOption("09:00", LocalTime.of(9, 0)),
            SlotOption("09:30", LocalTime.of(9, 30)),
            SlotOption("10:00", LocalTime.of(10, 0)),
            SlotOption("10:30", LocalTime.of(10, 30)),
            SlotOption("11:00", LocalTime.of(11, 0)),
            SlotOption("14:00", LocalTime.of(14, 0)),
            SlotOption("14:30", LocalTime.of(14, 30)),
            SlotOption("15:00", LocalTime.of(15, 0)),
            SlotOption("15:30", LocalTime.of(15, 30))
        )
    }
}

@Composable
fun SlotGrid(
    slots: List<SlotOption>,
    bookedSlots: Set<String>,
    pastSlots: Set<String> = emptySet(),
    selectedSlot: String?,
    onSlotSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        val morningSlots = slots.filter { it.time.hour < 12 }
        val afternoonSlots = slots.filter { it.time.hour >= 12 }

        if (morningSlots.isNotEmpty()) {
            Text(
                text = "Buổi sáng",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = HealthMuted,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            SlotRow(
                slots = morningSlots,
                bookedSlots = bookedSlots,
                pastSlots = pastSlots,
                selectedSlot = selectedSlot,
                onSlotSelected = onSlotSelected
            )
        }

        if (afternoonSlots.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Buổi chiều",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = HealthMuted,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            SlotRow(
                slots = afternoonSlots,
                bookedSlots = bookedSlots,
                pastSlots = pastSlots,
                selectedSlot = selectedSlot,
                onSlotSelected = onSlotSelected
            )
        }
    }
}

@Composable
private fun SlotRow(
    slots: List<SlotOption>,
    bookedSlots: Set<String>,
    pastSlots: Set<String>,
    selectedSlot: String?,
    onSlotSelected: (String) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.height(((slots.size / 3 + 1) * 44).dp)
    ) {
        items(slots) { slot ->
            SlotChip(
                label = slot.label,
                isBooked = bookedSlots.contains(slot.label),
                isPast = pastSlots.contains(slot.label),
                isSelected = selectedSlot == slot.label,
                onClick = { onSlotSelected(slot.label) }
            )
        }
    }
}

@Composable
private fun SlotChip(
    label: String,
    isBooked: Boolean,
    isPast: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val isDisabled = isBooked || isPast

    val backgroundColor = when {
        isBooked -> HealthMuted.copy(alpha = 0.15f)
        isPast -> Color(0xFFF5F5F5)
        isSelected -> HealthPrimary
        else -> HealthSurface
    }
    val contentColor = when {
        isBooked -> HealthMuted
        isPast -> Color(0xFFBDBDBD)
        isSelected -> Color.White
        else -> HealthPrimary
    }
    val borderColor = when {
        isBooked || isPast -> Color.Transparent
        isSelected -> HealthPrimary
        else -> HealthPrimary.copy(alpha = 0.4f)
    }

    Surface(
        onClick = if (isDisabled) ({}) else onClick,
        shape = RoundedCornerShape(10.dp),
        color = backgroundColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isPast) "$label •" else label,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor
            )
        }
    }
}
