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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vetbook.domain.models.NotificationType
import com.example.vetbook.presentation.viewmodels.NotificationUiItem
import com.example.vetbook.presentation.viewmodels.NotificationViewModel
import com.example.vetbook.presentation.viewmodels.ProfileViewModel

@Composable
fun NotificationScreen(
    onBackClick: () -> Unit = {},
    profileViewModel: ProfileViewModel = hiltViewModel(),
    notificationViewModel: NotificationViewModel = hiltViewModel()
) {
    val uiState by profileViewModel.uiState.collectAsState()
    val currentUserId = uiState.user?.id
    val notifications by notificationViewModel.notifications.collectAsState()

    LaunchedEffect(currentUserId) {
        if (currentUserId != null) {
            notificationViewModel.loadNotifications(currentUserId)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No notifications yet",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = notifications,
                    key = { it.notification.id }
                ) { uiItem ->
                    
                    // Mark as read immediately when rendered on screen if unread
                    LaunchedEffect(uiItem.notification.id, uiItem.notification.isRead) {
                        if (!uiItem.notification.isRead) {
                            notificationViewModel.markAsRead(uiItem.notification.id)
                        }
                    }
                    
                    NotificationCard(
                        uiItem = uiItem,
                        onDismiss = {
                            notificationViewModel.dismissNotification(uiItem.notification.id)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun NotificationCard(
    uiItem: NotificationUiItem,
    onDismiss: () -> Unit = {}
) {
    val notification = uiItem.notification
    val isUnread = !notification.isRead

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnread) Color(0xFFF0F8FF) else Color.White
        ),
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
                        text = "${notification.appName} • ${uiItem.timeAgo}",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
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
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = notification.title,
                    fontSize = 16.sp,
                    fontWeight = if (isUnread) FontWeight.ExtraBold else FontWeight.Bold,
                    color = Color.Black,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
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
