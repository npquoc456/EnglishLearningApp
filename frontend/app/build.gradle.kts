import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

// Local debug: phoneApiHost=<laptop LAN IP> in frontend/local.properties.
// Production: publicApiBaseUrl=https://api.your-domain.example/ (HTTPS, trailing slash).
// Default is the deployed HTTPS endpoint on AWS; override locally for development.
val localConfig = Properties().apply {
    val propsFile = rootProject.file("local.properties")
    if (propsFile.exists()) propsFile.inputStream().use { load(it) }
}
val phoneApiHost = localConfig.getProperty("phoneApiHost", "127.0.0.1").trim()
val publicApiBaseUrl = localConfig.getProperty("publicApiBaseUrl", "https://api.54.254.11.111.sslip.io/").trim()
if (publicApiBaseUrl.isNotEmpty()) {
    require(publicApiBaseUrl.startsWith("https://") &&
            publicApiBaseUrl.endsWith("/") &&
            !publicApiBaseUrl.contains('"') &&
            !publicApiBaseUrl.contains('\\')) {
        "publicApiBaseUrl must be a valid HTTPS base URL ending in '/'"
    }
}


android {
    namespace = "com.example.laptrinhungdungdidong"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.laptrinhungdungdidong"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        buildConfigField("String", "PHONE_API_BASE_URL", "\"http://$phoneApiHost:8080/\"")
        buildConfigField("String", "PUBLIC_API_BASE_URL", "\"$publicApiBaseUrl\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            // Local emulator/laptop HTTP debugging is allowed only in debug builds.
            manifestPlaceholders["allowCleartextTraffic"] = "true"
        }
        release {
            manifestPlaceholders["allowCleartextTraffic"] = "false"
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.recyclerview)

    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.kotlinx.coroutines.android)

    // ML Kit Text Recognition for Camera/Document OCR
    implementation("com.google.mlkit:text-recognition:16.0.1")

    // Google ML Kit On-Device Translation (English <-> Vietnamese)
    implementation("com.google.mlkit:translate:17.0.3")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
