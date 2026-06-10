package com.example.skinappp.ui.inference

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skinappp.DigitClassifier
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import javax.inject.Inject

data class InferenceUiState(
    val bitmap: Bitmap? = null,
    val heatmapBitmap: Bitmap? = null,
    val predictions: List<Pair<String, Float>> = emptyList(),
    val isAnalyzing: Boolean = false,
    val isGeneratingHeatmap: Boolean = false,
    val showHeatmap: Boolean = false,
    val ishalucinating : Boolean = false,
    val error: String? = null
)

@HiltViewModel
class InferenceViewModel @Inject constructor(
    private val digitClassifier: DigitClassifier
) : ViewModel() {

    private val _uiState = MutableStateFlow(InferenceUiState())
    val uiState: StateFlow<InferenceUiState> = _uiState.asStateFlow()

    fun onImageSelected(bitmap: Bitmap) {
        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(224, 224, ResizeOp.ResizeMethod.BILINEAR))
            .build()

        var tensorImage = TensorImage(DataType.FLOAT32)
        tensorImage.load(bitmap)
        tensorImage = imageProcessor.process(tensorImage)

        val scaledBitmap = tensorImage.bitmap
        _uiState.value = InferenceUiState(bitmap = scaledBitmap)
    }

    fun analyzeImage() {
        val bmp = _uiState.value.bitmap ?: return
        viewModelScope.launch(Dispatchers.Default) {
            _uiState.value = _uiState.value.copy(isAnalyzing = true, error = null)
            try {
                val predictions = digitClassifier.classify(bmp)
                _uiState.value = _uiState.value.copy(isAnalyzing = false, predictions = predictions)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isAnalyzing = false, error = e.message ?: "Analysis failed")
            }
        }
    }

    fun toggleHeatmap(enabled: Boolean) {
        if (enabled && _uiState.value.heatmapBitmap == null) {
            val bmp = _uiState.value.bitmap ?: return
            viewModelScope.launch(Dispatchers.Default) {
                _uiState.value = _uiState.value.copy(isGeneratingHeatmap = true, showHeatmap = true)
                try {
                    val (_, heatmap , ishalucinating ) = digitClassifier.generateExplainableHeatmap(bmp)
                    _uiState.value = _uiState.value.copy(heatmapBitmap = heatmap, isGeneratingHeatmap = false , ishalucinating = ishalucinating)
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(isGeneratingHeatmap = false, showHeatmap = false, error = "Heatmap generation failed")
                }
            }
        } else {
            _uiState.value = _uiState.value.copy(showHeatmap = enabled)
        }
    }

    fun reset() {
        _uiState.value = InferenceUiState()
    }

    override fun onCleared() {
        super.onCleared()
        digitClassifier.close()
    }
}
