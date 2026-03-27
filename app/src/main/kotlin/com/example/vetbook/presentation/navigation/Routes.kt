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
    object Security : Routes("security")
    object HelpSupport : Routes("help_support")
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
    object Products : Routes("products?category={category}") {
        fun createRoute(category: String? = null) = if (category != null) "products?category=$category" else "products"
    }
    object Cart : Routes("cart")
    object Payment : Routes("payment")
    object PaymentResult : Routes("payment_result/{isSuccess}?source={source}") {
        fun createRoute(isSuccess: Boolean, source: String = "store") =
            "payment_result/$isSuccess?source=$source"
    }
    object InAppPayment : Routes("in_app_payment?url={url}") {
        fun createRoute(url: String) = "in_app_payment?url=${java.net.URLEncoder.encode(url, "UTF-8")}"
    }
    object ProductDetail : Routes("product_detail/{productId}") {
        fun createRoute(productId: String) = "product_detail/$productId"
    }
    object OrderHistory : Routes("order_history")
    object OrderDetail : Routes("order_detail/{orderId}") {
        fun createRoute(orderId: String) = "order_detail/$orderId"
    }
    object Notifications : Routes("notifications")
    object EditProfile : Routes("edit_profile")
    object Language : Routes("language")
    object PrivacyPolicy : Routes("privacy_policy")
    object Accommodation : Routes("accommodation")
    object AccommodationDetail : Routes("accommodation_detail/{accommodationId}") {
        fun createRoute(accommodationId: String) = "accommodation_detail/$accommodationId"
    }
    object Pet : Routes("pet")
    object AddPet : Routes("add_pet?petId={petId}") {
        fun createRoute(petId: String? = null) = if (petId != null) "add_pet?petId=$petId" else "add_pet"
    }

    object ContinueLogin : Routes("continue_login")
    object ContinueLoginStart : Routes("continue_login_start")
    object ContinueLoginPassword : Routes("continue_login_password")

    // Vaccination screens
    object VaccinationList : Routes("vaccination_list/{petId}/{petName}?petType={petType}&birthDate={birthDate}") {
        fun createRoute(petId: String, petName: String, petType: String, birthDate: Long?) =
            "vaccination_list/$petId/$petName?petType=$petType&birthDate=${birthDate ?: ""}"
    }
    object AddVaccination : Routes("add_vaccination/{petId}/{petName}") {
        fun createRoute(petId: String, petName: String) = "add_vaccination/$petId/$petName"
    }
    object VaccinationDetail : Routes("vaccination_detail/{vaccinationId}") {
        fun createRoute(vaccinationId: String) = "vaccination_detail/$vaccinationId"
    }
    object Onboarding : Routes("onboarding")
}