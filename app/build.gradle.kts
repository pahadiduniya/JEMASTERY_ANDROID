plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.android")
  id("org.jetbrains.kotlin.plugin.compose")
  id("com.google.gms.google-services")
}

android {
  namespace = "com.jemastery.app"
  compileSdk = 34

  defaultConfig {
    applicationId = "com.jemastery.app"
    minSdk = 24
    targetSdk = 34
    versionCode = 2
    versionName = "2.0.1"
    buildConfigField("String", "WEB_CLIENT_ID", "\"12053815921-v4jqri32h260pcvio1r6uote59cbnvm4.apps.googleusercontent.com\"")
    buildConfigField("String", "PROJECT_ID", "\"je-mastery-360\"")
  }

  signingConfigs {
    create("jemastery") {
      storeFile = file("debug.keystore")
      storePassword = "jemastery123"
      keyAlias = "jemastery"
      keyPassword = "jemastery123"
    }
  }

  buildTypes {
    getByName("debug") {
      signingConfig = signingConfigs.getByName("jemastery")
    }
    getByName("release") {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.getByName("jemastery")
    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
  kotlinOptions { jvmTarget = "17" }
  buildFeatures { compose = true; buildConfig = true }
  packaging { resources.excludes += "/META-INF/{AL2.0,LGPL2.1}" }
}

dependencies {
  implementation(platform("androidx.compose:compose-bom:2024.09.03"))
  implementation("androidx.compose.ui:ui")
  implementation("androidx.compose.ui:ui-graphics")
  implementation("androidx.compose.material3:material3")
  implementation("androidx.activity:activity-compose:1.9.2")
  implementation("androidx.navigation:navigation-compose:2.8.1")
  implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.6")
  implementation("androidx.core:core-ktx:1.13.1")

  implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
  implementation("com.google.firebase:firebase-auth-ktx")
  implementation("com.google.android.gms:play-services-auth:21.2.0")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")
}
