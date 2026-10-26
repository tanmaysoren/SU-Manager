// The module is a library built inside another app (SU-Manager embeds it and opens its
// screens from its Labs page), so it builds with the same toolchain versions the host
// build uses: AGP 8.13.2 to match the host's Gradle, and the Kotlin Android plugin which
// AGP 9 carries built in but 8.13 does not. Everything that belonged to being an APK -
// the application id, the signing configs, the google-services and secrets boilerplate
// for capabilities the generated template shipped but the code never used - is gone.
plugins {
  // No versions here, on purpose: the composite build (AppManager + the Shizuku manager
  // checkout) already carries AGP and Kotlin 2.2.x on its plugin classpath, and asking
  // for a versioned copy inside the included build cannot be checked against what is
  // already loaded. The Kotlin plugin is also not declared in this file's settings,
  // again following the Shizuku-Next pattern - the version that runs is the one the
  // composite loaded, which is exactly what one shared APK wants. The one plugin nobody
  // else uses, KSP, takes its version from the settings file.
  id("com.android.library")
  id("org.jetbrains.kotlin.android")
  id("org.jetbrains.kotlin.plugin.compose")
  id("com.google.devtools.ksp")
}

android {
  namespace = "com.example"
  compileSdk = 36

  defaultConfig {
    minSdk = 24

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures {
    compose = true
  }
  testOptions { unitTests { isIncludeAndroidResources = true } }
}

// The Java side is 11; Kotlin 2.2 would otherwise pick 21 and the two compilers would
// refuse each other's output.
kotlin {
  compilerOptions {
    jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
  }
}

dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.play.services.location)
  "ksp"(libs.androidx.room.compiler)

  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.compose)
  testImplementation(libs.roborazzi.junit.rule)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
}
