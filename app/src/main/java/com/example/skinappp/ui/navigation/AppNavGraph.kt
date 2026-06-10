package com.example.skinappp.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.skinappp.domain.repository.AuthRepository
import com.example.skinappp.ui.appointments.AppointmentConfirmationScreen
import com.example.skinappp.ui.appointments.AppointmentDetailScreen
import com.example.skinappp.ui.appointments.BookAppointmentScreen
import com.example.skinappp.ui.appointments.MyAppointmentsScreen
import com.example.skinappp.ui.auth.LoginScreen
import com.example.skinappp.ui.auth.SignupScreen
import com.example.skinappp.ui.doctors.DoctorDetailScreen
import com.example.skinappp.ui.doctors.DoctorListScreen
import com.example.skinappp.ui.home.HomeScreen
import com.example.skinappp.ui.inference.InferenceScreen
import com.example.skinappp.ui.medication.MedicationConfirmationScreen
import com.example.skinappp.ui.profile.ProfileScreen
import com.example.skinappp.ui.theme.TealPrimary
import com.example.skinappp.ui.theme.TealSecondary

data class BottomNavItem(val label: String, val icon: ImageVector, val screen: Screen)

val bottomNavItems = listOf(
    BottomNavItem("Home", Icons.Filled.Home, Screen.Home),
    BottomNavItem("Appointments", Icons.Filled.CalendarMonth, Screen.MyAppointments),
    BottomNavItem("Profile", Icons.Filled.Person, Screen.Profile),
)

val bottomNavRoutes = setOf(Screen.Home.route, Screen.MyAppointments.route, Screen.Profile.route)

@Composable
fun AppNavGraph(authRepository: AuthRepository) {
    val navController = rememberNavController()
    val startDestination = if (authRepository.isLoggedIn()) Screen.Home.route else Screen.Login.route
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val showBottomBar = currentDestination?.route in bottomNavRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = TealPrimary
                    ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any { it.route == item.screen.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.screen.route)
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.LightGray,
                                unselectedIconColor = Color.LightGray,
                                selectedTextColor = Color.LightGray,       // Color of the text label when selected
                                unselectedTextColor = Color.LightGray, // Color of the text label when not selected
                                indicatorColor = TealSecondary
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToSignup = { navController.navigate(Screen.Signup.route) }
                )
            }
            composable(Screen.Signup.route) {
                SignupScreen(
                    onSignupSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Home.route) {
                HomeScreen(
                    onStartAnalysis = { navController.navigate(Screen.Inference.route) },
                    onViewAllAppointments = { navController.navigate(Screen.MyAppointments.route) },
                    onAppointmentClick = { appointmentId ->
                        navController.navigate(Screen.AppointmentDetail.createRoute(appointmentId))
                    }
                )
            }
            composable(Screen.Inference.route) {
                InferenceScreen(
                    onFindDoctors = { navController.navigate(Screen.DoctorList.route) }
                )
            }
            composable(Screen.DoctorList.route) {
                DoctorListScreen(
                    onDoctorClick = { doctorId ->
                        navController.navigate(Screen.DoctorDetail.createRoute(doctorId))
                    }
                )
            }
            composable(
                route = Screen.DoctorDetail.route,
                arguments = listOf(navArgument("doctorId") { type = NavType.StringType })
            ) { backStackEntry ->
                val doctorId = backStackEntry.arguments?.getString("doctorId") ?: ""
                DoctorDetailScreen(
                    doctorId = doctorId,
                    onBookAppointment = { navController.navigate(Screen.BookAppointment.createRoute(it)) },
                    onNavigateBack = {}
                )
            }
            composable(
                route = Screen.BookAppointment.route,
                arguments = listOf(navArgument("doctorId") { type = NavType.StringType })
            ) { backStackEntry ->
                val doctorId = backStackEntry.arguments?.getString("doctorId") ?: ""
                BookAppointmentScreen(
                    doctorId = doctorId,
                    onBookingConfirmed = { appointmentId ->
                        navController.navigate(Screen.AppointmentConfirmation.createRoute(appointmentId)) {
                            popUpTo(Screen.Home.route)
                        }
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(
                route = Screen.AppointmentConfirmation.route,
                arguments = listOf(navArgument("appointmentId") { type = NavType.StringType })
            ) { backStackEntry ->
                val appointmentId = backStackEntry.arguments?.getString("appointmentId") ?: ""
                AppointmentConfirmationScreen(
                    appointmentId = appointmentId,
                    onGoHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(
                route = Screen.AppointmentDetail.route,
                arguments = listOf(navArgument("appointmentId") { type = NavType.StringType })
            ) { backStackEntry ->
                val appointmentId = backStackEntry.arguments?.getString("appointmentId") ?: ""
                AppointmentDetailScreen(
                    appointmentId = appointmentId,
                    onNavigateBack = { navController.popBackStack() },
                    onReschedule = { doctorId -> 
                        navController.navigate(Screen.BookAppointment.createRoute(doctorId)) 
                    },
                    onAnalyzePrescription = { aptId ->
                        navController.navigate(Screen.MedicationConfirmation.createRoute(aptId))
                    }
                )
            }
            composable(
                route = Screen.MedicationConfirmation.route,
                arguments = listOf(navArgument("appointmentId") { type = NavType.StringType })
            ) { backStackEntry ->
                val appointmentId = backStackEntry.arguments?.getString("appointmentId") ?: ""
                MedicationConfirmationScreen(
                    appointmentId = appointmentId,
                    onNavigateBack = { navController.popBackStack() },
                    onSchedulingComplete = {
                        // After scheduling, pop back to appointment detail
                        navController.popBackStack()
                    }
                )
            }
            composable(Screen.MyAppointments.route) {
                MyAppointmentsScreen(
                    onAppointmentClick = { appointmentId ->
                        navController.navigate(Screen.AppointmentDetail.createRoute(appointmentId))
                    }
                )
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    onLogout = {

                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
