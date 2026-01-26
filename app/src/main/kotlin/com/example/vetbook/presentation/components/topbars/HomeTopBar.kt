package com.example.vetbook.presentation.components.topbars

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vetbook.R
import com.example.vetbook.presentation.components.SearchBar

@Composable
fun HomeTopBar(
    hasNotification: Boolean,
    onNotificationClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    TopBarSurface(
        modifier = modifier,
        backgroundColor = Color(0xFFFFD813) // Yellow background from Figma
    ) {
        // Location and profile row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Location selector
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { }
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Location",
                    tint = Color.Black,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Your Location",
                        fontSize = 12.sp,
                        color = Color.Black
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Ho Chi Minh City",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Dropdown",
                            modifier = Modifier.size(24.dp),
                            tint = Color.Black
                        )
                    }
                }
            }

            // Notification and profile
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile icon button
                IconButton(
                    onClick = onProfileClick,
                    modifier = Modifier
                        .size(42.dp)
                        .background(
                            Color.White.copy(alpha = 0.68f),
                            CircleShape
                        )
                ) {
                    Image(
                        painter = painterResource(R.drawable.pawns),
                        contentDescription = "Profile",
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                // Notification icon button
                Box {
                    IconButton(
                        onClick = onNotificationClick,
                        modifier = Modifier
                            .size(42.dp)
                            .background(
                                Color.White.copy(alpha = 0.68f),
                                CircleShape
                            )
                    ) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            modifier = Modifier.size(24.dp),
                            tint = Color.Black
                        )
                    }
                    if (hasNotification) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(9.dp)
                                .background(Color.Red, CircleShape)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        SearchBar()

        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Preview
@Composable
fun TopBarPreview() {
    HomeTopBar(hasNotification = true)
}
