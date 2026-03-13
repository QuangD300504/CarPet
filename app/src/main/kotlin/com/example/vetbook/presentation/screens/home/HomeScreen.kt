package com.example.vetbook.presentation.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vetbook.domain.models.Banner
import com.example.vetbook.domain.models.ServiceCategory
import com.example.vetbook.presentation.components.CaringBanner
import com.example.vetbook.presentation.components.RecommendedServices
import com.example.vetbook.presentation.components.ServiceCategoriesSection
import com.example.vetbook.presentation.components.SponsoredSection
import com.example.vetbook.presentation.models.HomeUiState
import com.example.vetbook.presentation.previews.PreviewNavScaffold
import com.example.vetbook.presentation.components.topbars.HomeTopBar
import com.example.vetbook.presentation.theme.HealthPrimary
import com.example.vetbook.presentation.theme.HealthMuted
import com.example.vetbook.presentation.theme.TextPrimary
import com.example.vetbook.presentation.theme.TextSecondary
import com.example.vetbook.presentation.theme.Background
import com.example.vetbook.presentation.viewmodels.HomeViewModel
import java.util.Calendar

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onSeeAllClick: () -> Unit,
    onCategoryClick: (ServiceCategory) -> Unit,
    userName: String? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val banners by viewModel.banners.collectAsState()

    HomeContent(
        uiState = uiState,
        categories = categories,
        banners = banners,
        userName = userName,
        onCategoryClick = onCategoryClick,
        onSeeAllClick = onSeeAllClick
    )
}

@Composable
fun HomeContent(
    uiState: HomeUiState,
    categories: List<ServiceCategory>,
    banners: List<Banner> = emptyList(),
    userName: String? = null,
    onCategoryClick: (ServiceCategory) -> Unit,
    modifier: Modifier = Modifier,
    onSeeAllClick: () -> Unit = {}
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Background),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            GreetingSection(userName = userName)
        }

        item { CaringBanner() }

        item {
            ServiceCategoriesSection(
                categories = categories,
                onCategoryClick = onCategoryClick,
                onViewAllClick = onSeeAllClick
            )
        }

        item {
            RecommendedServices(
                categories = categories,
                onCategoryClick = onCategoryClick,
                onSeeAllClick = onSeeAllClick
            )
        }

        item {
            SponsoredSection(banners = banners)
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Bạn đã xem hết tin mới! ✨",
                    fontSize = 14.sp,
                    color = HealthMuted,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun GreetingSection(userName: String?) {
    val greeting = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 0..11 -> "Chào buổi sáng"
        in 12..16 -> "Chào buổi chiều"
        else -> "Chào buổi tối"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Text(
            text = greeting,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = HealthMuted
        )
        Text(
            text = if (userName.isNullOrBlank()) "Chào mừng bạn!" else "Xin chào, $userName! 👋",
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimary,
            letterSpacing = (-0.5).sp
        )
    }
}

@Composable
fun previewHomeCategories() = listOf(
    ServiceCategory("cat_vet", "Khám bệnh", "Chăm sóc y tế chuyên nghiệp cho thú cưng", com.example.vetbook.R.drawable.services),
    ServiceCategory("cat_hotel", "Khách sạn", "Nơi lưu trú an toàn và thoải mái", com.example.vetbook.R.drawable.services),
    ServiceCategory("cat_walk", "Dắt đi dạo", "Vận động cùng thú cưng năng động", com.example.vetbook.R.drawable.services),
    ServiceCategory("cat_shop", "Cửa hàng", "Phụ kiện thú cưng chất lượng cao", com.example.vetbook.R.drawable.store)
)

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun HomeScreenPreview() {
    PreviewNavScaffold(
        topBar = {
            HomeTopBar(
                currentLocation = "Hồ Chí Minh",
                onLocationClick = {},
                onCartClick = {},
                onNotificationClick = {},
                onProfileClick = {},
                searchPlaceholder = "Tìm kiếm dịch vụ...",
                searchValue = "",
                onSearchChange = {}
            )
        }
    ) { padding ->
        HomeContent(
            uiState = HomeUiState(),
            categories = previewHomeCategories(),
            banners = emptyList(),
            userName = "Quang",
            onCategoryClick = {},
            onSeeAllClick = {},
            modifier = Modifier.padding(padding)
        )
    }
}
