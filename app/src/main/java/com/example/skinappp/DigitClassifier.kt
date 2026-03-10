package com.example.skinappp

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer

class DigitClassifier(private val context: Context) {
    private var interpreter: Interpreter? = null
    private val modelName = "skin_disease_model.tflite"

    // Match this list EXACTLY to your Python CLASS_NAMES (Alphabetical order!)
    private val labels = listOf(
        "Acne", "Benign Keratosis", "Carcinoma", "Chickenpox",
        "Dermato Fibroma", "Dyshidrotic Eczema", "Melanoma",
        "Nail Fungus", "Nevus", "Normal Skin", "Ringworm",
        "Squamous Risk", "Vascular Lesion"
    )

    init {
        val mappedFile = FileUtil.loadMappedFile(context, modelName)
        val options = Interpreter.Options()
        interpreter = Interpreter(mappedFile, options)
    }

    fun classify(bitmap: Bitmap): String {
        // 1. Ensure ARGB_8888 (TFLite needs 4 channels converted to 3 internally or manual RGB)
        // TensorImage handles the stripping of Alpha channel automatically for FLOAT32 models usually
        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(224, 224, ResizeOp.ResizeMethod.BILINEAR))
            // 🔴 CHECK: If your Python training used `1./255` scaling, KEEP THIS.
            // If you used EfficientNet without scaling, REMOVE THIS.
       //     .add(NormalizeOp(0.0f, 255.0f))
            .build()

        var tensorImage = TensorImage(DataType.FLOAT32)
        tensorImage.load(bitmap)
        tensorImage = imageProcessor.process(tensorImage)

        // 2. Prepare Output Buffer
        // Shape: [1, 13]
        val outputBuffer = TensorBuffer.createFixedSize(intArrayOf(1, 13), DataType.FLOAT32)

        // 3. Run Inference
        interpreter?.run(tensorImage.buffer, outputBuffer.buffer.rewind())

        // 4. Process Results
        val probabilities = outputBuffer.floatArray
        val maxIndex = probabilities.indices.maxByOrNull { probabilities[it] } ?: -1

        return if (maxIndex != -1) {
            val className = labels[maxIndex]
            val confidence = probabilities[maxIndex] * 100

            // Helpful Tip for UI:
            if (confidence < 50) {
                "⚠️ Low Confidence\nBest Guess: $className (%.1f%%)".format(confidence)
            } else {
                "Result: $className\nConfidence: %.1f%%".format(confidence)
            }
        } else {
            "Error: Could not classify"
        }
    }

    fun close() {
        interpreter?.close()
    }
}