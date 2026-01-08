package com.example.carpet.presentation.navigation

sealed class Routes(val route: String) {
    object Login : Routes("login")
    object SignUp : Routes("signup")
    object Home : Routes("home")
    object Service : Routes("service")
    object Community : Routes("community")
    object Profile : Routes("profile")

    object ServiceDetail : Routes("service_detail/{serviceId}") {
        fun createRoute(serviceId: String) = "service_detail/$serviceId"
    }
    object PetProfile : Routes("pet_profile/{petId}") {
        fun createRoute(petId: String) = "pet_profile/$petId"
    }
}