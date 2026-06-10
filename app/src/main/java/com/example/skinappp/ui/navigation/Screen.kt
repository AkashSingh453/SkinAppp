package com.example.skinappp.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Signup : Screen("signup")
    object Home : Screen("home")
    object Inference : Screen("inference")
    object DoctorList : Screen("doctor_list")
    object DoctorDetail : Screen("doctor_detail/{doctorId}") {
        fun createRoute(doctorId: String) = "doctor_detail/$doctorId"
    }
    object BookAppointment : Screen("book_appointment/{doctorId}") {
        fun createRoute(doctorId: String) = "book_appointment/$doctorId"
    }
    object AppointmentConfirmation : Screen("appointment_confirmation/{appointmentId}") {
        fun createRoute(appointmentId: String) = "appointment_confirmation/$appointmentId"
    }
    object AppointmentDetail : Screen("appointment_detail/{appointmentId}") {
        fun createRoute(appointmentId: String) = "appointment_detail/$appointmentId"
    }
    object MedicationConfirmation : Screen("medication_confirmation/{appointmentId}") {
        fun createRoute(appointmentId: String) = "medication_confirmation/$appointmentId"
    }
    object MyAppointments : Screen("my_appointments")
    object Profile : Screen("profile")
}
