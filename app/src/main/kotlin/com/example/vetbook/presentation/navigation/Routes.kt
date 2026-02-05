package com.example.vetbook.presentation.navigation

sealed class Routes(val route: String) {
    object Splash : Routes("splash")
    object Login : Routes("login")
    object SignUp : Routes("signup")
    object ForgotPassword : Routes("forgot_password")
    object Home : Routes("home")
    object Services : Routes("services")
    object Calendar : Routes("calendar")
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
    object BookAppointment : Routes("book_appointment/{doctorId}") {
        fun createRoute(doctorId: String) = "book_appointment/$doctorId"
    }
    object Store : Routes("store")
    object Products : Routes("products")
    object Cart : Routes("cart")
    object Payment : Routes("payment")
    object Notifications : Routes("notifications")
    object EditProfile : Routes("edit_profile")
    object Language : Routes("language")
    object PrivacyPolicy : Routes("privacy_policy")
    object Accommodation : Routes("accommodation")
    object Pet : Routes("pet")
    object AddPet : Routes("add_pet")

    object ContinueLogin : Routes("continue_login")
    object ContinueLoginStart : Routes("continue_login_start")
    object ContinueLoginPassword : Routes("continue_login_password")
}
