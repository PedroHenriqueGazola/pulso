plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.android")
  id("org.jetbrains.kotlin.plugin.compose")
}

android {
  namespace = "app.pulso.watch"
  compileSdk = 35

  defaultConfig {
    applicationId = "app.pulso.watch"
    minSdk = 33
    targetSdk = 35
    versionCode = 1
    versionName = "0.1.0"
  }

  buildTypes {
    release {
      isMinifyEnabled = false
    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  buildFeatures {
    compose = true
  }

  testOptions {
    unitTests.isReturnDefaultValues = true
  }
}

kotlin {
  jvmToolchain(17)
}

dependencies {
  implementation(platform("androidx.compose:compose-bom:2025.06.01"))
  implementation("androidx.compose.ui:ui")
  implementation("androidx.wear.compose:compose-material3:1.5.0")
  implementation("androidx.wear.compose:compose-foundation:1.5.0")
  implementation("androidx.activity:activity-compose:1.10.1")
  implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.1")
  implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.1")
  implementation("androidx.lifecycle:lifecycle-service:2.9.1")
  implementation("androidx.datastore:datastore-preferences:1.1.7")
  implementation("androidx.core:core-ktx:1.16.0")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")

  testImplementation("junit:junit:4.13.2")
  testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
}
