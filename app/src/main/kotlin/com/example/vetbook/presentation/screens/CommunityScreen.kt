package com.example.vetbook.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.example.vetbook.domain.models.Pet
import com.example.vetbook.domain.models.PetEvent
import com.example.vetbook.domain.models.Post
import com.example.vetbook.presentation.models.CommunityTab
import com.example.vetbook.presentation.viewmodels.CommunityViewModel

@Composable
fun CommunityScreen(
    modifier: Modifier = Modifier,
    viewModel: CommunityViewModel = hiltViewModel(),
    onAdoptClick: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFDFDFD))
    ) {
        Surface(shadowElevation = 0.dp) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF5F5F5)),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    CommunityTabItem(
                        title = "Feed",
                        isSelected = uiState.selectedTab == CommunityTab.Feed,
                        onClick = { viewModel.onTabSelected(CommunityTab.Feed) },
                        modifier = Modifier.weight(1f)
                    )
                    CommunityTabItem(
                        title = "Adoption",
                        isSelected = uiState.selectedTab == CommunityTab.Adoption,
                        onClick = { viewModel.onTabSelected(CommunityTab.Adoption) },
                        modifier = Modifier.weight(1f)
                    )
                    CommunityTabItem(
                        title = "Events",
                        isSelected = uiState.selectedTab == CommunityTab.Events,
                        onClick = { viewModel.onTabSelected(CommunityTab.Events) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        when (uiState.selectedTab) {
            CommunityTab.Feed -> FeedList(
                posts = uiState.posts,
                onLikeClick = { },
                onCommentClick = { },
                onShareClick = { }
            )
            CommunityTab.Adoption -> AdoptionList(
                pets = uiState.pets,
                onAdoptClick = onAdoptClick
            )
            CommunityTab.Events -> EventList(uiState.events)
        }
    }
}

@Composable
fun CommunityTabItem(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) Color.White else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.Black.copy(alpha = 1f) else Color.Gray,
            fontSize = 14.sp
        )
    }
}

@Composable
fun FeedList(
    posts: List<Post>,
    onLikeClick: (Post) -> Unit,
    onCommentClick: (Post) -> Unit,
    onShareClick: (Post) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(posts) { post ->
            PostCard(
                post = post,
                onLikeClick = { onLikeClick(post) },
                onCommentClick = { onCommentClick(post) },
                onShareClick = { onShareClick(post) }
            )
        }
    }
}

@Composable
fun PostCard(
    post: Post,
    onLikeClick: () -> Unit = {},
    onCommentClick: () -> Unit = {},
    onShareClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFE0B2)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "👩", fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = post.authorName, fontWeight = FontWeight.Bold, color = Color.Black.copy(alpha = 1f))
                    Text(text = post.timestamp, fontSize = 12.sp, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (post.imageUrl != null) {
                AsyncImage(
                    model = post.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Text(text = post.content, fontSize = 14.sp, color = Color.Black.copy(alpha = 1f))

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFFF0F0F0))
            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Row(
                        modifier = Modifier.clickable { onLikeClick() }.padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.FavoriteBorder,
                            contentDescription = "Like",
                            modifier = Modifier.size(20.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = post.likesCount.toString(), fontSize = 12.sp, color = Color.Gray)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Row(
                        modifier = Modifier.clickable { onCommentClick() }.padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ChatBubbleOutline,
                            contentDescription = "Comment",
                            modifier = Modifier.size(20.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = post.commentsCount.toString(), fontSize = 12.sp, color = Color.Gray)
                    }
                }
                
                IconButton(onClick = onShareClick) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = "Share",
                        modifier = Modifier.size(20.dp),
                        tint = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun AdoptionList(
    pets: List<Pet>,
    onAdoptClick: (String) -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(pets) { pet ->
            AdoptionCard(
                pet = pet,
                onAdoptClick = { onAdoptClick(pet.id) }
            )
        }
    }
}

@Composable
fun AdoptionCard(
    pet: Pet,
    onAdoptClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF5F5F5)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = if (pet.type.lowercase() == "dog") "🐕" else "🐱", fontSize = 40.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = pet.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black.copy(alpha = 1f))
                Text(text = pet.breed, color = Color.Gray, fontSize = 14.sp)
                Text(text = pet.age, color = Color.Gray, fontSize = 14.sp)

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onAdoptClick,
                    modifier = Modifier.height(36.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                ) {
                    Text(text = "Adopt Now", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun EventList(events: List<PetEvent>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(events) { event ->
            EventCard(event)
        }
    }
}

@Composable
fun EventCard(event: PetEvent) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "🎉", fontSize = 32.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = event.title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black.copy(alpha = 1f))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = event.date, color = Color.Gray, fontSize = 14.sp)
            Text(text = event.location, color = Color.Gray, fontSize = 14.sp)

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { },
                modifier = Modifier.height(40.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(text = "Join Event", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CommunityScreenPreview() {
    CommunityScreen()
}
