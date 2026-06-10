# 🔬 Dermat AI: Edge-Optimized Skin Disease Classifier

## Description
Dermat AI is an advanced, privacy-first Android application designed for real-time dermatological diagnostics. By leveraging edge computing, the app processes camera frames locally on the device to classify 13 different skin conditions with high accuracy (~90%). Because inference happens entirely offline, user data and sensitive health images never leave the device, ensuring complete privacy and low latency.


## 📸 Screenshots

| | | | | |
|:---:|:---:|:---:|:---:|:---:|
| <img src="https://github.com/user-attachments/assets/bd983bbd-31b3-46d4-b44e-c93fd698fc14" width="180"> | <img src="https://github.com/user-attachments/assets/203041d8-4a46-4263-aa55-0113c4e6c97f" width="180"> | <img src="https://github.com/user-attachments/assets/5dc090d9-ea17-4412-87cd-483ae39d7461" width="180"> | <img src="https://github.com/user-attachments/assets/28134c7c-c023-4553-891b-c650433ad3d9" width="180"> | <img src="https://github.com/user-attachments/assets/25b05343-7bbb-4a88-a169-9d26b39f1f19" width="180"> |
| <img src="https://github.com/user-attachments/assets/5c111011-059d-4bd2-8e69-a57de74e333b" width="180"> | <img src="https://github.com/user-attachments/assets/b751a23e-e8b4-4249-81d7-921dc13b9534" width="180"> | <img src="https://github.com/user-attachments/assets/e741c323-569c-4512-b961-d5f09da43462" width="180"> | <img src="https://github.com/user-attachments/assets/9bcebd98-77e4-4cb8-acd8-fb44af6b9930" width="180"> | <img src="https://github.com/user-attachments/assets/04b18754-09a3-4e62-b6f2-10450c8ba53b" width="180"> |
| <img src="https://github.com/user-attachments/assets/90d3eec0-ec8f-459d-a8d4-fe34cbe7dbb0" width="180"> | <img src="https://github.com/user-attachments/assets/a48d77fd-b2b7-4771-acad-84807fc69bc6" width="180"> | <img src="https://github.com/user-attachments/assets/9716352f-09c6-41bf-8a23-56e8e6de2aff" width="180"> | <img src="https://github.com/user-attachments/assets/094f4621-6495-46a5-9a13-de8dd74428ae" width="180"> | <img src="https://github.com/user-attachments/assets/9367d167-8186-41da-9642-86ac72f211e0" width="180"> |

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
