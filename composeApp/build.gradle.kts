import java.io.File
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    kotlin("plugin.serialization") version "2.0.0"
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    jvm("desktop") {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    sourceSets {
        val commonMain by getting
        val androidMain by getting
        val desktopMain by getting
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.latex.renderer)
            implementation(libs.compose.icons.extended)
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
            implementation("io.ktor:ktor-client-core:2.3.11")
            implementation("io.ktor:ktor-client-cio:2.3.11")
            implementation("io.ktor:ktor-client-content-negotiation:2.3.11")
            implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.11")
        }

        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.activity.compose)
            implementation("com.google.ai.client.generativeai:generativeai:0.9.0")
            implementation("androidx.camera:camera-core:1.3.4")
            implementation("androidx.camera:camera-camera2:1.3.4")
            implementation("androidx.camera:camera-lifecycle:1.3.4")
            implementation("androidx.camera:camera-view:1.3.4")
            implementation("com.google.accompanist:accompanist-permissions:0.34.0")
            implementation("io.ktor:ktor-client-android:2.3.11")
        }

        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
        }
    }
}

android {
    namespace = "org.infa252.project"
    compileSdk = 36

    defaultConfig {
        applicationId = "org.infa252.project"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        val groqKey = File(rootProject.projectDir, "local.properties")
            .takeIf { it.exists() }
            ?.readLines()
            ?.find { it.startsWith("GROQ_API_KEY=") }
            ?.substringAfter("=")
            ?.trim() ?: ""

        buildConfigField("String", "GROQ_API_KEY", "\"$groqKey\"")

        externalNativeBuild {
            cmake {
                cppFlags("-std=c++17")
            }
        }
    }

    buildFeatures {
        buildConfig = true
    }

    externalNativeBuild {
        cmake {
            path = file("../core-engine/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(libs.compose.uiTooling)
}

compose.desktop {
    application {
        mainClass = "org.infa252.project.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "org.infa252.project"
            packageVersion = "1.0.0"
        }
    }
}