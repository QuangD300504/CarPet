package com.example.vetbook.domain.repository

import com.example.vetbook.domain.models.Banner
import kotlinx.coroutines.flow.Flow

interface BannerRepository {
    fun getBanners(): Flow<List<Banner>>
    suspend fun getBannerById(id: String): Banner?
    suspend fun addBanner(banner: Banner): Result<Banner>
    suspend fun updateBanner(id: String, fields: Map<String, Any?>)
    suspend fun deleteBanner(id: String)
}
