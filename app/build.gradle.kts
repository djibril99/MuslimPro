
plugins {
    alias(libs.plugins.com.android.application)
    alias(libs.plugins.org.jetbrains.kotlin.android)
    id("org.jetbrains.kotlin.kapt") version "1.5.31"
}

android{
    namespace = "com.example.muslimpro"
    compileSdk = 33

 defaultConfig {
        applicationId = "com.example.muslimpro"
        minSdk = 21
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
            sourceCompatibility = JavaVersion.VERSION_1_8
            targetCompatibility = JavaVersion.VERSION_1_8
        }
        kotlinOptions {
            jvmTarget = "1.8"
        }
        buildFeatures {
            compose = true
        }
        composeOptions {
            kotlinCompilerExtensionVersion = "1.3.2"
        }
        packaging {
            resources {
                excludes += "/META-INF/{AL2.0,LGPL2.1}"
            }
        }
/*
    buildscript {
        repositories {
            google()
            mavenCentral()
        }
        dependencies {
            classpath("com.android.tools.build:gradle:7.3.0")
        }
    }
*/
}

dependencies {

    implementation(libs.core.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.material3)
    implementation(libs.androidx.room.common)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.ui.test.junit4)
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)

    //dependances pour les composants :
    implementation("androidx.compose.material3:material3:1.0.0-alpha06")
    implementation("androidx.compose.foundation:foundation:1.0.0-alpha06")
    implementation("androidx.compose.runtime:runtime-livedata:1.0.0-alpha06")
    implementation("androidx.compose.runtime:runtime-rxjava2:1.0.0-alpha06")
    implementation("androidx.compose.runtime:runtime:1.0.0-alpha06")
    implementation("androidx.compose.ui:ui:1.0.0-alpha06")
    implementation("androidx.compose.ui:ui-tooling:1.0.0-alpha06")
    // pour le menu hamburger
    implementation("androidx.compose.material:material-icons-core:1.1.0")
    implementation("androidx.compose.material:material-icons-extended:1.1.0")

    //pour la base de donnée
    implementation("androidx.sqlite:sqlite:2.3.1")


    implementation("androidx.room:room-runtime:2.5.1")
    implementation("androidx.room:room-rxjava2:2.5.1")
    implementation("androidx.room:room-ktx:2.5.1")
    kapt("androidx.room:room-compiler:2.5.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.2")


}