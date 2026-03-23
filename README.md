# 🔬 Dermat AI: Edge-Optimized Skin Disease Classifier

## Description
Dermat AI is an advanced, privacy-first Android application designed for real-time dermatological diagnostics. By leveraging edge computing, the app processes camera frames locally on the device to classify 13 different skin conditions with high accuracy (~90%). Because inference happens entirely offline, user data and sensitive health images never leave the device, ensuring complete privacy and low latency.

## ✨ Key Features
* **Real-Time Inference:** Seamlessly integrates the Android Camera API with TensorFlow Lite, utilizing background thread executors to process frames asynchronously without dropping UI performance.
* **Clinical-Grade Preprocessing:** Implements an OpenCV-based hair-removal algorithm directly within the data pipeline to eliminate visual artifacts and improve prediction accuracy.
* **Explainable AI (XAI):** Validated using Occlusion Sensitivity Mapping to ensure the neural network extracts features from actual lesions rather than background noise.
* **Privacy-First Architecture:** 100% on-device processing. No cloud reliance, no data harvesting.
* **Modern UI:** Built entirely with Jetpack Compose for a reactive, smooth, and accessible user experience.

## 🧠 Machine Learning Architecture
* **Model:** Fine-tuned lightweight MobileNetV2 (optimized via transfer learning).
* **Training Techniques:** Applied Categorical Focal Loss to stabilize training across severely imbalanced minority classes in a complex dataset of 40,000+ images.
* **Deployment:** Quantized and converted to `.tflite` for mobile edge deployment.

## 🛠️ Tech Stack
* **Android / UI:** Kotlin, Jetpack Compose
* **Machine Learning:** Python, TensorFlow, TensorFlow Lite, Keras
* **Computer Vision:** OpenCV
* **Concurrency:** Kotlin Coroutines & Background Executors

---

## 🚀 Installation & Setup

1. Clone the repository:
   ```bash
   git clone [https://github.com/AkashSingh453/SkinAppp.git](https://github.com/AkashSingh453/SkinAppp.git)
