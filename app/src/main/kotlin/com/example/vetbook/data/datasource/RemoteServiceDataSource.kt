package com.example.vetbook.data.datasource

import com.example.vetbook.data.models.PetServiceDetailDto
import com.example.vetbook.data.models.ServiceCategoryDto
import com.example.vetbook.data.models.ServicePackageDto

/**
 * Remote data source for service category and package data.
 * Handles fetching service information from Firestore including subcollections.
 */
interface RemoteServiceDataSource {
    
    /**
     * Get all service categories.
     * @return List of service categories
     */
    suspend fun getServiceCategories(): List<ServiceCategoryDto>
    
    /**
     * Get a specific service category by ID.
     * @param categoryId The service category identifier
     * @return The service category data or null if not found
     */
    suspend fun getServiceCategoryById(categoryId: String): ServiceCategoryDto?
    
    /**
     * Get service packages for a specific category from subcollection.
     * @param categoryId The service category identifier
     * @return List of packages for the category
     */
    suspend fun getServicePackages(categoryId: String): List<ServicePackageDto>
    
    /**
     * Get complete service detail including packages.
     * @param categoryId The service category identifier
     * @return Complete service detail with packages or null if not found
     */
    suspend fun getServiceDetail(categoryId: String): PetServiceDetailDto?
    
    /**
     * Create a new service category.
     * @param service The service category data to create
     * @return Result containing the created service or error
     */
    suspend fun createServiceCategory(service: ServiceCategoryDto): Result<ServiceCategoryDto>
    
    /**
     * Create a service package in a category's subcollection.
     * @param categoryId The parent service category ID
     * @param package The package data to create
     * @return Result containing the created package or error
     */
    suspend fun createServicePackage(categoryId: String, `package`: ServicePackageDto): Result<ServicePackageDto>
}
