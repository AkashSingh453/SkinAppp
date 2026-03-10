package com.example.skinappp

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.skinappp.data.Resource
import com.example.skinappp.model.SavedAddress
import com.example.skinappp.ui.theme.SkinApppTheme
import com.example.skinappp.viewModels.AddressViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import kotlin.toString
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.util.Log
import android.widget.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.privacysandbox.tools.core.model.Method
import com.example.skinappp.ApiService.BackendApiService
import com.example.skinappp.ml.Mobilenet
import com.example.skinappp.viewModels.BackendViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    val addViewModel: AddressViewModel by viewModels()
    val gpsLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            addViewModel.fetchLoc() // Retry after user turns on GPS
        }
    }

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
//            val addViewModel: AddressViewModel = hiltViewModel()
//            LaunchedEffect(Unit) {
//                addViewModel.gpsLaunchSignal.collect { intentSenderRequest ->
//                    gpsLauncher.launch(intentSenderRequest)
//                }
//            }
            val modelName = "skin_disease_model.tflite"
            val context = LocalContext.current
            val classifier = DigitClassifier(context)

            SkinApppTheme {
                Column(modifier = Modifier.fillMaxSize()) {
              //      Greeting()
                    ImagePickerScreen(classifier)
              //      extracted(context)
                }
            }
        }
    }

    @Composable
    private fun extracted(context: Context) {
        HorizontalDivider(
            thickness = 50.dp
        )
        val BackendViewModel: BackendViewModel = hiltViewModel()
        val backMessage by BackendViewModel.data.collectAsState()

        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia(),
            onResult = { uri ->
                uri?.let {
                    val bitmap = try {
                        MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                    } catch (e: Exception) {
                        null
                    }
                    val stream = ByteArrayOutputStream()
                    val success =
                        bitmap?.compress(Bitmap.CompressFormat.JPEG, 90, stream)
                    if (success == true) {
                        val byteArray = stream.toByteArray()
                        BackendViewModel.sendAuth(
                            byteArray.toRequestBody(
                                "image/jpeg".toMediaTypeOrNull(),
                                0,
                                byteArray.size
                            )
                        )
                    }
                }
            }
        )

        Button(onClick = {
            launcher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }) {
            Text("send to server")
        }
        when (backMessage) {
            is Resource.Loading -> {
                Text("mrfl")
            }

            is Resource.Error -> {
                Text((backMessage as Resource.Error).exception.message.toString())
            }

            is Resource.Success -> {
                Text((backMessage as Resource.Success).data.toString())
            }
        }
    }
}

@Composable
fun Greeting(
    addViewModel: AddressViewModel = hiltViewModel()
) {
    val res by addViewModel.data.collectAsState()
    val sa by addViewModel.sa.collectAsState()
    val context = LocalContext.current
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val isGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (isGranted) {
            // Permission Granted: Tell ViewModel to go to work!
            addViewModel.fetchLoc()
        } else {
            Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }) {
            Text("Get Address")
        }
        when (res) {
            is Resource.Loading -> {
                Text("jndsk")
            }

            is Resource.Error -> {
                val errorMsg = (res as Resource.Error).exception.message
                Text(errorMsg.toString())
            }

            is Resource.Success -> {
                val data_ = (res as Resource.Success).data.features.get(0).properties
                Text(data_.city)
            }
        }
        when (sa) {
            is Resource.Loading -> {
                Text("Loading")
            }

            is Resource.Error -> {
                Text((sa as Resource.Error).exception.message.toString())
            }

            is Resource.Success -> {
                (sa as Resource.Success<List<SavedAddress>>).data.forEach {
                    Card(
                        modifier = Modifier.clickable(
                            onClick = {
                                addViewModel.deltAddr(it)
                            }
                        )) {
                        Text(text = it.toString())
                    }
                }

            }
        }
    }
}

@Composable
fun ImagePickerScreen(classifier: DigitClassifier) {
    val context = LocalContext.current
    val model = Mobilenet.newInstance(context)


    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var tempUri by remember { mutableStateOf<Uri?>(null) }
    var resultText by remember { mutableStateOf("Select an image to classify") }
    val coroutinescope = rememberCoroutineScope()
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            imageUri = uri
            uri?.let {
                val inputSize = 1 * 224 * 224 * 3 * 4
                var buffer : ByteBuffer = ByteBuffer.allocateDirect(inputSize).apply {
                    // TFLite requires the Native Byte Order (usually Little Endian)
                    order(ByteOrder.nativeOrder())
                }
                coroutinescope.launch {
                    val buff = coroutinescope.async { ImageUtils(context).uriToByteBuffer(uri) }
                    buffer = buff.await()
                }
                val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), DataType.FLOAT32)
                inputFeature0.loadBuffer(buffer)
                val bitmap = try {
                    if (Build.VERSION.SDK_INT < 28) {
                        MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                    } else {
                        val source = ImageDecoder.createSource(context.contentResolver, it)
                        ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                            // Add this to prevent the "invalid input" error
                            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                            decoder.isMutableRequired = true
                        }
                    }
                } catch (e: Exception) {
                    null
                }
                bitmap?.let { b ->
                    resultText = classifier.classify(b)
                }
            }
        }
    )

    val cameralauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { succ ->
            if (succ) {
                imageUri = tempUri
                val bitmap = try {
                    if (Build.VERSION.SDK_INT < 28) {
                        MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
                    } else {
                        val source = ImageDecoder.createSource(context.contentResolver, imageUri!!)
                        ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                            // Add this to prevent the "invalid input" error
                            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                            decoder.isMutableRequired = true
                        }
                    }
                } catch (e: Exception) {
                    null
                }
                bitmap?.let { b ->
                    resultText = classifier.classify(b)
                }
            }
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = {
            launcher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }) {
            Text("Select Image from Gallery")
        }
        Button(onClick = {
            // 3. Create a temporary file and get its Uri via FileProvider
            val uri = ComposeFileProvider.getImageUri(context)
            tempUri = uri
            cameralauncher.launch(uri)
        }) {
            Text("Take Photo for Analysis")
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Display the selected image
        imageUri?.let {
            AsyncImage(
                model = it,
                contentDescription = null,
                modifier = Modifier
                    .size(224.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(text = resultText, style = MaterialTheme.typography.bodyLarge)
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


class ImageUtils(private val context: Context) {

    suspend fun uriToByteBuffer(uri: Uri): ByteBuffer = withContext(Dispatchers.IO) {
        // 1. Open an input stream from the Uri
        val inputStream = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        // 2. Prepare the ImageProcessor (Matches your 224x224 model input)
        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(224, 224, ResizeOp.ResizeMethod.BILINEAR))
            // Add NormalizeOp here if your model requires it (e.g., mean 127.5, std 127.5)
            .build()

        // 3. Convert Bitmap to TensorImage
        var tensorImage = TensorImage(DataType.FLOAT32)
        tensorImage.load(bitmap)
        tensorImage = imageProcessor.process(tensorImage)

        // 4. Return the underlying ByteBuffer
        return@withContext tensorImage.buffer
    }
}
