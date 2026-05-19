package com.example.skinappp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.skinappp.data.dto.Coordinates
import com.example.skinappp.data.dto.Fees
import com.example.skinappp.data.remote.SkinApiService
import com.example.skinappp.domain.repository.AuthRepository
import com.example.skinappp.ui.auth.AuthViewModel
import com.example.skinappp.ui.doctors.DoctorViewModel
import com.example.skinappp.ui.navigation.AppNavGraph
import com.example.skinappp.ui.theme.SkinApppTheme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SkinApppTheme {
               AppNavGraph(authRepository = authRepository)
        //        DoctorSendScreen()
            }
        }
    }
}

@Composable
fun DoctorSendScreen(
    viewModel: AuthViewModel = hiltViewModel()
){
    Button(
        onClick = {
           dummyDoctors.forEach {
               viewModel.addDoctor(it)
           }
        }
    ){
        Text("Send Doctors To database");
    }
}

@Serializable
data class DoctorCreateRequest(
    @SerialName("doctor_name") val doctorName: String,
    @SerialName("phone_number") val phoneNumber: String,
    @SerialName("experience_years") val experienceYears: Int,
    val biography: String,
    @SerialName("profile_image_url") val profileImageUrl: String? = null,
    val rating: Double = 0.0,
    val location: String,
    val coordinates: Coordinates,
    val fees: Fees,
    @SerialName("working_hours") val workingHours: Map<String, List<String>> = emptyMap()
)


val dummyDoctors = listOf(
    DoctorCreateRequest(
        doctorName = "Dr. Aman Verma",
        phoneNumber = "9876500001",
        experienceYears = 12,
        biography = "Experienced cardiologist specializing in preventive heart care.",
        profileImageUrl = null,
        rating = 4.7,
        location = "Pandri, Raipur",
        coordinates = Coordinates(21.2514, 81.6296),
        fees = Fees(700),
        workingHours = mapOf(
            "Monday" to listOf("10:00 AM - 2:00 PM", "5:00 PM - 8:00 PM"),
            "Tuesday" to listOf("10:00 AM - 2:00 PM")
        )
    ),
    DoctorCreateRequest(
        doctorName = "Dr. Neha Sharma",
        phoneNumber = "9876500002",
        experienceYears = 8,
        biography = "Dermatologist with expertise in skin allergies and cosmetic treatments.",
        profileImageUrl = null,
        rating = 4.5,
        location = "Shankar Nagar, Raipur",
        coordinates = Coordinates(21.2510, 81.6675),
        fees = Fees(500),
        workingHours = mapOf(
            "Monday" to listOf("9:00 AM - 1:00 PM"),
            "Wednesday" to listOf("4:00 PM - 7:00 PM")
        )
    ),
    DoctorCreateRequest(
        doctorName = "Dr. Ravi Tiwari",
        phoneNumber = "9876500003",
        experienceYears = 15,
        biography = "Orthopedic surgeon focusing on sports injuries and fractures.",
        profileImageUrl = null,
        rating = 4.8,
        location = "Telibandha, Raipur",
        coordinates = Coordinates(21.2395, 81.7012),
        fees = Fees(900),
        workingHours = mapOf(
            "Tuesday" to listOf("11:00 AM - 3:00 PM"),
            "Friday" to listOf("6:00 PM - 9:00 PM")
        )
    ),
    DoctorCreateRequest(
        doctorName = "Dr. Priya Nair",
        phoneNumber = "9876500004",
        experienceYears = 10,
        biography = "Gynecologist and women's wellness consultant.",
        profileImageUrl = null,
        rating = 4.6,
        location = "Civil Lines, Raipur",
        coordinates = Coordinates(21.2408, 81.6521),
        fees = Fees(650),
        workingHours = mapOf(
            "Monday" to listOf("10:00 AM - 1:00 PM"),
            "Thursday" to listOf("5:00 PM - 8:00 PM")
        )
    ),
    DoctorCreateRequest(
        doctorName = "Dr. Saurabh Mishra",
        phoneNumber = "9876500005",
        experienceYears = 7,
        biography = "General physician providing family healthcare services.",
        profileImageUrl = null,
        rating = 4.3,
        location = "Mowa, Raipur",
        coordinates = Coordinates(21.2618, 81.7140),
        fees = Fees(400),
        workingHours = mapOf(
            "Monday" to listOf("9:00 AM - 12:00 PM"),
            "Saturday" to listOf("5:00 PM - 8:00 PM")
        )
    ),

    DoctorCreateRequest(
        doctorName = "Dr. Kunal Agrawal",
        phoneNumber = "9876500006",
        experienceYears = 11,
        biography = "Pediatrician with expertise in child nutrition and vaccinations.",
        profileImageUrl = null,
        rating = 4.7,
        location = "Devendra Nagar, Raipur",
        coordinates = Coordinates(21.2558, 81.6402),
        fees = Fees(550),
        workingHours = mapOf(
            "Tuesday" to listOf("10:00 AM - 2:00 PM"),
            "Friday" to listOf("4:00 PM - 7:00 PM")
        )
    ),

    DoctorCreateRequest(
        doctorName = "Dr. Simran Kaur",
        phoneNumber = "9876500007",
        experienceYears = 9,
        biography = "ENT specialist experienced in sinus and hearing disorders.",
        profileImageUrl = null,
        rating = 4.4,
        location = "Samta Colony, Raipur",
        coordinates = Coordinates(21.2443, 81.6419),
        fees = Fees(600),
        workingHours = mapOf(
            "Monday" to listOf("11:00 AM - 3:00 PM")
        )
    ),

    DoctorCreateRequest(
        doctorName = "Dr. Aditya Rao",
        phoneNumber = "9876500008",
        experienceYears = 14,
        biography = "Neurologist focusing on migraine and epilepsy treatment.",
        profileImageUrl = null,
        rating = 4.9,
        location = "VIP Road, Raipur",
        coordinates = Coordinates(21.2527, 81.7345),
        fees = Fees(1200),
        workingHours = mapOf(
            "Wednesday" to listOf("10:00 AM - 1:00 PM"),
            "Saturday" to listOf("5:00 PM - 9:00 PM")
        )
    ),

    DoctorCreateRequest(
        doctorName = "Dr. Harshita Jain",
        phoneNumber = "9876500009",
        experienceYears = 6,
        biography = "Dentist specializing in cosmetic and restorative dentistry.",
        profileImageUrl = null,
        rating = 4.2,
        location = "Tatibandh, Raipur",
        coordinates = Coordinates(21.2844, 81.6903),
        fees = Fees(450),
        workingHours = mapOf(
            "Monday" to listOf("9:00 AM - 12:00 PM"),
            "Thursday" to listOf("4:00 PM - 8:00 PM")
        )
    ),

    DoctorCreateRequest(
        doctorName = "Dr. Rohit Sen",
        phoneNumber = "9876500010",
        experienceYears = 18,
        biography = "Senior pulmonologist with ICU and respiratory care experience.",
        profileImageUrl = null,
        rating = 4.8,
        location = "Pachpedi Naka, Raipur",
        coordinates = Coordinates(21.2239, 81.6535),
        fees = Fees(1000),
        workingHours = mapOf(
            "Tuesday" to listOf("10:00 AM - 1:00 PM"),
            "Friday" to listOf("6:00 PM - 8:00 PM")
        )
    ),

    // Naya Raipur Entries

    DoctorCreateRequest(
        doctorName = "Dr. Meenal Kapoor",
        phoneNumber = "9876500011",
        experienceYears = 13,
        biography = "Psychiatrist specializing in stress and anxiety management.",
        profileImageUrl = null,
        rating = 4.7,
        location = "Sector 27, Naya Raipur",
        coordinates = Coordinates(21.1612, 81.7870),
        fees = Fees(850),
        workingHours = mapOf(
            "Monday" to listOf("11:00 AM - 2:00 PM")
        )
    ),

    DoctorCreateRequest(
        doctorName = "Dr. Vivek Dubey",
        phoneNumber = "9876500012",
        experienceYears = 9,
        biography = "Diabetologist helping patients manage chronic diabetes care.",
        profileImageUrl = null,
        rating = 4.5,
        location = "Sector 24, Naya Raipur",
        coordinates = Coordinates(21.1704, 81.7762),
        fees = Fees(700),
        workingHours = mapOf(
            "Tuesday" to listOf("10:00 AM - 2:00 PM")
        )
    ),

    DoctorCreateRequest(
        doctorName = "Dr. Nidhi Sinha",
        phoneNumber = "9876500013",
        experienceYears = 5,
        biography = "Young physiotherapist specializing in rehabilitation therapy.",
        profileImageUrl = null,
        rating = 4.1,
        location = "Sector 19, Naya Raipur",
        coordinates = Coordinates(21.1791, 81.7640),
        fees = Fees(350),
        workingHours = mapOf(
            "Monday" to listOf("8:00 AM - 12:00 PM")
        )
    ),

    DoctorCreateRequest(
        doctorName = "Dr. Rajeev Khanna",
        phoneNumber = "9876500014",
        experienceYears = 16,
        biography = "Senior nephrologist with kidney disease management expertise.",
        profileImageUrl = null,
        rating = 4.8,
        location = "Sector 21, Naya Raipur",
        coordinates = Coordinates(21.1735, 81.7723),
        fees = Fees(1100),
        workingHours = mapOf(
            "Wednesday" to listOf("10:00 AM - 1:00 PM")
        )
    ),

    DoctorCreateRequest(
        doctorName = "Dr. Pooja Bansal",
        phoneNumber = "9876500015",
        experienceYears = 8,
        biography = "Ophthalmologist treating cataract and retina conditions.",
        profileImageUrl = null,
        rating = 4.6,
        location = "Kamal Vihar, Raipur",
        coordinates = Coordinates(21.1902, 81.6820),
        fees = Fees(650),
        workingHours = mapOf(
            "Friday" to listOf("10:00 AM - 3:00 PM")
        )
    )

    // Continue similarly for remaining entries till 30...
)


