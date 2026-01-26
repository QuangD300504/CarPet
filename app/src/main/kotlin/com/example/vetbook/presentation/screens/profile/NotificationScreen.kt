package com.example.vetbook.presentation.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vetbook.presentation.models.Notification
import com.example.vetbook.presentation.models.NotificationType

@Composable
fun NotificationScreen(
    onBackClick: () -> Unit = {},
    onNotificationDismiss: (String) -> Unit = {}
) {
    var notifications by remember {
        mutableStateOf(
            listOf(
                Notification(
                    id = "1",
                    appName = "APP NAME",
                    timeAgo = "NOW",
                    title = "New notification",
                    description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore.",
                    type = NotificationType.INFO,
                    isRead = false
                ),
                Notification(
                    id = "2",
                    appName = "APP NAME",
                    timeAgo = "3 MIN AGO",
                    title = "Incident - High severity",
                    description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore.",
                    type = NotificationType.INCIDENT,
                    isRead = false
                ),
                Notification(
                    id = "3",
                    appName = "APP NAME",
                    timeAgo = "NOW",
                    title = "Jane Doe replied to your post about so and so",
                    description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore.",
                    type = NotificationType.REPLY,
                    isRead = false
                )
            )
        )
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Yellow header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFFEB3B))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.Black
                )
            }
            Text(
                text = "NOTI",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        
        // Notifications list
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = notifications,
                key = { it.id }
            ) { notification ->
                NotificationCard(
                    notification = notification,
                    onDismiss = {
                        notifications = notifications.filter { it.id != notification.id }
                        onNotificationDismiss(notification.id)
                    }
                )
            }
        }
    }
}

@Composable
fun NotificationCard(
    notification: Notification,
    onDismiss: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Left indicator/icon
            when (notification.type) {
                NotificationType.INCIDENT -> {
                    // Red bar and icon
                    Column(
                        modifier = Modifier.width(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .background(Color(0xFFE53935))
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFFE53935), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Incident",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                else -> {
                    // Blue icon
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFF2196F3).copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Info",
                            tint = Color(0xFF2196F3),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
            
            // Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "${notification.appName} • ${notification.timeAgo}",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.weight(1f)
                    )
                    if (notification.type != NotificationType.INCIDENT) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss",
                                tint = Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = notification.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = notification.description,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NotificationScreenPreview() {
    NotificationScreen()
}

