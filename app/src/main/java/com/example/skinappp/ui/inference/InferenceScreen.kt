package com.example.skinappp.ui.inference

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.skinappp.ui.theme.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.io.File

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun InferenceScreen(
    onFindDoctors: () -> Unit,
    viewModel: InferenceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showSourceDialog by remember { mutableStateOf(false) }
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let {
                val bmp = MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                viewModel.onImageSelected(bmp)
            }
        }
    )
    val imageUri = remember { mutableStateOf<Uri?>(ComposeFileProvider.getImageUri(context)) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { ctx ->
            if (ctx){
                imageUri.value.let {
                    val bmp = MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                    viewModel.onImageSelected(bmp)
                }
            }
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(TealPrimary, TealSecondary),
                        Offset(0f, 0f),
                        Offset(Float.POSITIVE_INFINITY, 200f)
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Text(
                    "Skin Analysis",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Upload or capture a skin image for AI diagnosis",
                    color = Color.White.copy(0.85f),
                    fontSize = 13.sp
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // Image area
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clickable {
                        showSourceDialog = true
                               },
                contentAlignment = Alignment.Center
            ) {
                val displayBitmap =
                    if (uiState.showHeatmap && uiState.heatmapBitmap != null) uiState.heatmapBitmap else uiState.bitmap
                if (displayBitmap != null) {
                    Image(
                        bitmap = displayBitmap.asImageBitmap(),
                        contentDescription = "Selected skin image",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(20.dp)),
                        contentScale = ContentScale.Crop
                    )
                    // Retake overlay
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(TealPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(TealSoft),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.AddAPhoto,
                                null,
                                tint = TealPrimary,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Tap to add image",
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            fontSize = 16.sp
                        )
                        Text("Gallery or Camera", color = TextSecondary, fontSize = 13.sp)
                    }
                }
            }
        }

        // XAI switch (only when results available)
        AnimatedVisibility(visible = uiState.predictions.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Show AI Focus Area",
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        "Highlights important skin regions",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    if (uiState.ishalucinating ){
                        Text(
                            "Model Seems To Be Hallucinating",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
                if (uiState.isGeneratingHeatmap) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = TealPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Switch(
                        checked = uiState.showHeatmap,
                        onCheckedChange = { viewModel.toggleHeatmap(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = TealPrimary
                        )
                    )
                }
            }
        }

        // Analyze button
        AnimatedVisibility(visible = uiState.bitmap != null && uiState.predictions.isEmpty()) {
            Button(
                onClick = { viewModel.analyzeImage() },
                enabled = !uiState.isAnalyzing,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
            ) {
                if (uiState.isAnalyzing) {
                    CircularProgressIndicator(
                        Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Analyzing...", fontWeight = FontWeight.SemiBold)
                } else {
                    Icon(Icons.Default.Search, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Analyze Skin", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                }
            }
        }

        // Results
        AnimatedVisibility(visible = uiState.predictions.isNotEmpty()) {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Analysis Results",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextPrimary
                )
                Spacer(Modifier.height(12.dp))

                uiState.predictions.forEachIndexed { index, (label, confidence) ->
                    val confFloat = confidence / 100f
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = if (index == 0) TealSoft else Color.White),
                        elevation = CardDefaults.cardElevation(if (index == 0) 4.dp else 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    label,
                                    fontWeight = if (index == 0) FontWeight.Bold else FontWeight.Medium,
                                    color = TextPrimary,
                                    fontSize = if (index == 0) 16.sp else 14.sp
                                )
                                Text(
                                    "${confidence.toInt()}%",
                                    fontWeight = FontWeight.Bold,
                                    color = TealPrimary,
                                    fontSize = 14.sp
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { confFloat.coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = if (index == 0) TealPrimary else TealSecondary,
                                trackColor = BorderSoft
                            )
                        }
                    }
                }
                MedicalDisclaimerCard()
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onFindDoctors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                ) {
                    Icon(Icons.Default.LocationOn, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Find Nearest Doctors", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Spacer(Modifier.height(24.dp))
            }
        }

        uiState.error?.let {
            Spacer(Modifier.height(12.dp))
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                color = MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    it,
                    modifier = Modifier.padding(14.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }

    if (showSourceDialog) {
        AlertDialog(
            onDismissRequest = { showSourceDialog = false },
            title = { Text("Select Image Source") },
            text = {
                Column {
                    ListItem(
                        headlineContent = { Text("Choose from Gallery") },
                        leadingContent = { Icon(Icons.Default.Photo, null, tint = TealPrimary) },
                        modifier = Modifier.clickable {
                            showSourceDialog = false;
                            galleryLauncher.launch( PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }
                    )
                    ListItem(
                        headlineContent = { Text("Take a Photo") },
                        leadingContent = {
                            Icon(
                                Icons.Default.CameraAlt,
                                null,
                                tint = TealPrimary
                            )
                        },
                        modifier = Modifier.clickable {
                            if (cameraPermission.status.isGranted) {
                                // We have permission! Safe to launch the camera
                                showSourceDialog = false;
                                cameraLauncher.launch(imageUri.value)
                            } else {
                                // We don't have permission yet. Ask the user for it!
                                cameraPermission.launchPermissionRequest()
                            }
                        }
                    )
                }
            },
            confirmButton = { TextButton({ showSourceDialog = false }) { Text("Cancel") } }
        )
    }
}

@Composable
fun MedicalDisclaimerCard(
    modifier: Modifier = Modifier
) {
    // Exact color matching for the warning banner
    val warningBackground = Color(0xFFFFF9E6) // Soft warm amber tint
    val warningBorder = Color(0xFFFFEAA7)     // Slightly darker amber line
    val warningText = Color(0xFF664D03)       // Deep brown-amber readable text

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(warningBackground)
            .border(1.dp, warningBorder, RoundedCornerShape(24.dp))
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalAlignment = Alignment.Top // Ensures the icon stays aligned at the top line of text
    ) {
        // Warning Emoji or Icon
        Text(
            text = "⚠️",
            fontSize = 18.sp,
            modifier = Modifier.padding(top = 2.dp) // Slight offset adjustment for emoji baseline alignment
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Disclaimer Text
        Text(
            text = "This is an AI prediction and should not replace professional medical advice. Please consult with a dermatologist for proper diagnosis.",
            color = warningText,
            fontSize = 15.sp,
            lineHeight = 22.sp
        )
    }
}


class ComposeFileProvider : FileProvider() {
    companion object {
        fun getImageUri(context: Context): Uri {
            val directory = File(context.cacheDir, "images")
            directory.mkdirs()
            val file = File.createTempFile("selected_image_", ".jpg", directory)
            val authority = "${context.packageName}.fileprovider"
            return getUriForFile(context, authority, file)
        }
    }
}
