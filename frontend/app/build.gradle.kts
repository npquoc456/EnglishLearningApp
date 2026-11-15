import java.io.File
import java.net.Socket
import java.util.concurrent.TimeUnit

plugins {
    alias(libs.plugins.android.application)
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

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        viewBinding = true
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

val backendDirectoryPath = rootProject.projectDir.parentFile.resolve("laptrinhungdungdidong").absolutePath

tasks.register("startBackendServer") {
    group = "application"
    description = "Checks and starts the Spring Boot backend server automatically before running frontend."
    val dirPath = backendDirectoryPath
    doLast {
        val port = 8080
        val isRunning = try {
            Socket("127.0.0.1", port).use { true }
        } catch (_: Exception) {
            false
        }

        if (isRunning) {
            println(">>> [Backend] Server is already active on http://localhost:$port")
        } else {
            println(">>> [Backend] Starting Spring Boot server automatically...")
            val backendDir = File(dirPath)
            val jarFile = File(backendDir, "build/libs/demo-0.0.1-SNAPSHOT.jar")

            if (!jarFile.exists()) {
                println(">>> [Backend] Building backend jar first...")
                val buildPb = ProcessBuilder("./gradlew", "bootJar", "-x", "test")
                buildPb.directory(backendDir)
                buildPb.inheritIO().start().waitFor()
            }

            val logFile = File(backendDir, "backend_runtime.log")
            val pb = ProcessBuilder("java", "-jar", jarFile.absolutePath)
            pb.directory(backendDir)
            pb.redirectOutput(ProcessBuilder.Redirect.appendTo(logFile))
            pb.redirectError(ProcessBuilder.Redirect.appendTo(logFile))
            pb.start()
            println(">>> [Backend] Process launched. Output redirected to: ${logFile.absolutePath}")

            var started = false
            for (i in 1..20) {
                Thread.sleep(500)
                try {
                    Socket("127.0.0.1", port).use { started = true }
                    break
                } catch (_: Exception) {}
            }
            if (started) {
                println(">>> [Backend] Backend successfully started on port $port!")
            } else {
                println(">>> [Backend] Backend is initializing in background...")
            }
        }

        try {
            val adbProcess = ProcessBuilder("adb", "reverse", "tcp:8080", "tcp:8080").start()
            adbProcess.waitFor(1, TimeUnit.SECONDS)
            println(">>> [ADB] Reverse port 8080 configured.")
        } catch (_: Exception) {}
    }
}

tasks.named("preBuild") {
    dependsOn("startBackendServer")
}
