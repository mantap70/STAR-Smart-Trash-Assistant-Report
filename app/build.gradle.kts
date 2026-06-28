import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

android {
    namespace = "com.mantao.star"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.mantao.star"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // API key dibaca dari local.properties (file ini TIDAK di-commit ke git)
        buildConfigField(
            "String",
            "GROQ_API_KEY",
            "\"${localProperties.getProperty("GROQ_API_KEY", "")}\""
        )
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // CoordinatorLayout untuk activity_main
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")

    // TensorFlow Lite untuk klasifikasi sampah (fitur Scan)
    implementation("org.tensorflow:tensorflow-lite:2.14.0")
    implementation("org.tensorflow:tensorflow-lite-support:0.4.4")

    // OkHttp untuk koneksi ke Groq API (fitur Chatbot)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Room untuk menyimpan laporan secara lokal (fitur Report)
    implementation("androidx.room:room-runtime:2.8.4")
    annotationProcessor("androidx.room:room-compiler:2.8.4")

    // RecyclerView untuk halaman History
    implementation("androidx.recyclerview:recyclerview:1.4.0")

    // osmdroid untuk peta interaktif (fitur Locate) — gratis, tanpa API key
    implementation("org.osmdroid:osmdroid-android:6.1.20")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}