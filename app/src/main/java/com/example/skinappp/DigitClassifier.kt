package com.example.skinappp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import kotlin.math.max
import kotlin.math.min

// Renamed from DigitClassifier to reflect its actual purpose
class DigitClassifier(private val context: Context) {

    // 🛠 THE CRITICAL FIX: Use raw Interpreter instead of the buggy auto-generated class
    private val interpreter: Interpreter by lazy {
        val options = Interpreter.Options()
        Interpreter(loadModelFile(context, "Efficient.tflite"), options)
    }

    // Helper function to read the TFLite file directly from the assets folder
    private fun loadModelFile(context: Context, modelPath: String): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, fileDescriptor.startOffset, fileDescriptor.declaredLength)
    }

    // EXACT 7 classes from our Kaggle pipeline (Must remain exactly alphabetical)
    private val classNames = listOf(
        "Acne",
        "Healthy Nail",
        "Melanoma",
        "Nail Fungus",
        "Nevus",
        "Normal Skin",
        "Ringworm"
    )

    fun classify(bitmap: Bitmap): List<Pair<String, Float>> {
        // We can just reuse runInference here to keep the code DRY
        val probabilities = runInference(bitmap)
        return getTopPredictions(probabilities)
    }

    fun getTopPredictions(probabilities: FloatArray): List<Pair<String, Float>> {
        val sortedResults = probabilities.indices
            .map { index ->
                // Pair the class name with its confidence score (converted to percentage)
                classNames[index] to (probabilities[index] * 100)
            }
            .sortedByDescending { it.second }

        // 🛡 IMPLEMENTING SOLUTION A: The Out-of-Distribution Threshold
        val topPrediction = sortedResults.first()
        if (topPrediction.second < 60f) {
            // If the model is less than 60% sure, override the results
            return listOf(Pair("Unrecognized Feature - Please retake clearly", topPrediction.second))
        }

        // Otherwise, return the top 3 legitimate results
        return sortedResults.take(3)
    }

    private fun runInference(bitmap: Bitmap): FloatArray {
        // Use TensorFlow's mathematical resizer
        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(224, 224, ResizeOp.ResizeMethod.BILINEAR))
            .build()

        var tensorImage = TensorImage(DataType.FLOAT32)
        tensorImage.load(bitmap)
        tensorImage = imageProcessor.process(tensorImage)

        // Create an output buffer for our 7 classes
        val outputBuffer = TensorBuffer.createFixedSize(intArrayOf(1, classNames.size), DataType.FLOAT32)

        // Feed the perfectly processed Tensor buffer directly into the Interpreter!
        interpreter.run(tensorImage.buffer, outputBuffer.buffer.rewind())

        return outputBuffer.floatArray
    }
    private fun calculateMeanColor(bitmap: Bitmap): Int {
        var r = 0L; var g = 0L; var b = 0L
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        for (pixel in pixels) {
            r += Color.red(pixel)
            g += Color.green(pixel)
            b += Color.blue(pixel)
        }
        val count = pixels.size
        return Color.rgb((r / count).toInt(), (g / count).toInt(), (b / count).toInt())
    }

    suspend fun generateExplainableHeatmap(originalBitmap: Bitmap, gridSize: Int = 10): Triple<Bitmap, Bitmap , Boolean> {
        // Use TensorFlow's mathematical resizer instead of Android's native resizer
        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(224, 224, ResizeOp.ResizeMethod.BILINEAR))
            .build()

        var tensorImage = TensorImage(DataType.FLOAT32)
        tensorImage.load(originalBitmap)
        tensorImage = imageProcessor.process(tensorImage)

        val scaledBitmap = tensorImage.bitmap

        // 2. Get baseline prediction to see what the model thinks before we hide anything
        val baselineProbs = runInference(scaledBitmap)
        val baseClassIndex = baselineProbs.indices.maxByOrNull { baselineProbs[it] } ?: 0
        val baseConf = baselineProbs[baseClassIndex]

        val patchWidth = scaledBitmap.width / gridSize
        val patchHeight = scaledBitmap.height / gridSize
        val importanceMap = Array(gridSize) { FloatArray(gridSize) }

        var maxDrop = -1f
        var minDrop = 1f

        // 3. Calculate "Camouflage" color (average skin tone of this specific image)
        val meanSkinColor = calculateMeanColor(scaledBitmap)
        val workingBitmap = scaledBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(workingBitmap)
        val maskPaint = Paint().apply { color = meanSkinColor }

        // 4. Run the Occlusion Experiment (The 10x10 Loop)
        for (i in 0 until gridSize) {
            for (j in 0 until gridSize) {
                // Reset canvas
                canvas.drawBitmap(scaledBitmap, 0f, 0f, null)

                // Draw camouflage square
                val left = (j * patchWidth).toFloat()
                val top = (i * patchHeight).toFloat()
                canvas.drawRect(left, top, left + patchWidth, top + patchHeight, maskPaint)

                // Predict with the square hidden
                val newProbs = runInference(workingBitmap)
                val newConf = newProbs[baseClassIndex]

                // Record the confidence drop
                val drop = baseConf - newConf
                importanceMap[i][j] = drop

                if (drop > maxDrop) maxDrop = drop
                if (drop < minDrop) minDrop = drop
            }
        }

        val rawMaxDrop = maxDrop
        val isHallucinating = rawMaxDrop < 0.15f

        // 5. FIX 1: The Anchor (Prevent Noise Amplification)
        minDrop = min(0f, minDrop)
        maxDrop = max(0.15f, maxDrop) // Ignore tiny drops smaller than 15%

        val range = if (maxDrop - minDrop == 0f) 1f else (maxDrop - minDrop)

        // 6. Create a 10x10 math grid (Values strictly between 0.0 and 1.0)
        val normalizedMap = Array(gridSize) { FloatArray(gridSize) }
        for (i in 0 until gridSize) {
            for (j in 0 until gridSize) {
                normalizedMap[i][j] = (importanceMap[i][j] - minDrop) / range
            }
        }

        // 7. FIX 2: The Matplotlib Stretch (Bilinear Interpolation of the Math)
        val smoothHeatmap = Bitmap.createBitmap(224, 224, Bitmap.Config.ARGB_8888)
        val scale = gridSize.toFloat() / 224f

        for (y in 0 until 224) {
            for (x in 0 until 224) {
                // Map the 224x224 coordinate back down to the 10x10 grid
                val gx = (x + 0.5f) * scale - 0.5f
                val gy = (y + 0.5f) * scale - 0.5f

                val x0 = max(0, gx.toInt())
                val x1 = min(gridSize - 1, x0 + 1)
                val y0 = max(0, gy.toInt())
                val y1 = min(gridSize - 1, y0 + 1)

                val tx = gx - x0
                val ty = gy - y0

                // Get the math values of the 4 closest squares
                val v00 = normalizedMap[y0][x0]
                val v10 = normalizedMap[y0][x1]
                val v01 = normalizedMap[y1][x0]
                val v11 = normalizedMap[y1][x1]

                // Calculate the exact decimal curve between the squares
                val top = v00 * (1 - tx) + v10 * tx
                val bottom = v01 * (1 - tx) + v11 * tx
                val finalMathValue = top * (1 - ty) + bottom * ty

                // Color the pixel *after* the math is stretched
                // Alpha is set to 153 (60% opacity) for that perfect semi-transparent overlay
                smoothHeatmap.setPixel(x, y, getThermalColor(finalMathValue, 153))
            }
        }

        // 8. Blend the perfectly smooth heatmap directly over the skin image
        val blendedBitmap = scaledBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val blendCanvas = Canvas(blendedBitmap)
        blendCanvas.drawBitmap(smoothHeatmap, 0f, 0f, null)

        // Return the Original (top view) and the Blended Heatmap (bottom view)
        return Triple(scaledBitmap, blendedBitmap , isHallucinating)
    }
    private fun getThermalColor(value: Float, alpha: Int = 255): Int {
        val v = max(0f, min(1f, value))
        val r = (max(0f, min(1f, 1.5f - Math.abs(1f - 4f * (v - 0.5f)))) * 255).toInt()
        val g = (max(0f, min(1f, 1.5f - Math.abs(1f - 4f * (v - 0.25f)))) * 255).toInt()
        val b = (max(0f, min(1f, 1.5f - Math.abs(1f - 4f * v))) * 255).toInt()
        return Color.argb(alpha, r, g, b)
    }

    fun close() {
     //   interpreter.close()
    }

    private companion object {
        const val INPUT_SIZE = 224
        const val PIXEL_SIZE = 3
        const val FLOAT_SIZE_BYTES = 4
    }
}
