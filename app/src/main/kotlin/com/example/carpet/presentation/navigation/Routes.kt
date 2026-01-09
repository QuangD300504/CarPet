package com.example.carpet.presentation.navigation

sealed class Routes(val route: String) {
    object Splash : Routes("splash")
    object Login : Routes("login")
    object SignUp : Routes("signup")
    object ForgotPassword : Routes("forgot_password")
    object Home : Routes("home")
    object Service : Routes("service")
    object Community : Routes("community")
    object Profile : Routes("profile")
    object Veterinarians : Routes("veterinarians")
    object DoctorProfile : Routes("doctor_profile/{doctorId}") {
        fun createRoute(doctorId: String) = "doctor_profile/$doctorId"
    }

    object ServiceDetail : Routes("service_detail/{serviceId}") {
        fun createRoute(serviceId: String) = "service_detail/$serviceId"
    }
    object PetProfile : Routes("pet_profile/{petId}") {
        fun createRoute(petId: String) = "pet_profile/$petId"
    }
}
