pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    // 버전은 여기서만 관리 — :app이 제외된 환경(Android SDK 없음)에서는 AGP를 해석하지 않는다.
    plugins {
        id("com.android.application") version "8.7.3"
        id("org.jetbrains.kotlin.android") version "2.0.21"
        id("org.jetbrains.kotlin.jvm") version "2.0.21"
        id("org.jetbrains.kotlin.plugin.compose") version "2.0.21"
    }
}

plugins {
    // JDK 자동 프로비저닝(로컬에 요구 버전이 없을 때 다운로드)
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "loadcast-android"

include(":core")

// Android SDK가 있는 환경에서만 :app 포함 — SDK 없는 CI에서도 :core:test는 항상 실행 가능
val hasAndroidSdk = System.getenv("ANDROID_HOME") != null ||
    System.getenv("ANDROID_SDK_ROOT") != null ||
    java.io.File(rootDir, "local.properties").exists()
if (hasAndroidSdk) {
    include(":app")
}
