pluginManagement {
  repositories {
    google {
      content {
        includeGroupByRegex("com\\.android.*")
        includeGroupByRegex("com\\.google.*")
        includeGroupByRegex("androidx.*")
      }
    }
    mavenCentral()
    gradlePluginPortal()
  }
  plugins {
    // Versions are pinned here rather than applied in the settings script, the way the
    // Shizuku checkout next door does it: the Kotlin plugins refuse to be applied to the
    // Settings object, and KSP needs them on its classloader anyway. The Kotlin version
    // matches the one the composite already pins through that checkout; a different one
    // in the same Gradle run could not load. AGP takes the host's version regardless -
    // see the comment in app/build.gradle.kts.
    id("org.jetbrains.kotlin.android") version "2.2.20"
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.20"
    id("com.google.devtools.ksp") version "2.3.5"
  }
}

dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    google()
    mavenCentral()
  }
}

rootProject.name = "GPS & IP Spoofer"

include(":app")
