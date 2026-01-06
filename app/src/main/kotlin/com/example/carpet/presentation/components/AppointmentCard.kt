package com.example.carpet.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.carpet.R

@Composable
fun AppointmentCard() {
    Card(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            Icon(
                painter = painterResource(R.drawable.calender),
                contentDescription = null,
                tint = Color.Unspecified
            )
            Column() {
                Text("Lịch hẹn hôm nay", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Vet appointment: 3:00 PM, Nov 2")
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = { },
                    border = BorderStroke(1.dp, Color(0xFFFFA500)),
                ) {
                    Text(
                        text = "View Details",
                        color = Color(0xFFFFA500)
                    )
                }
            }
        }
    }
}


@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun AppointmentCardPreview() {
    AppointmentCard()
}