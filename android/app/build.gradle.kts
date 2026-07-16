import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

// 키는 커밋하지 않는 keys.properties(.gitignore)에서 읽어 BuildConfig로 주입.
// 파일이 없거나 값이 비어도 빌드는 성공한다(소셜 로그인만 비활성). 템플릿: keys.properties.example
val keys = Properties().apply {
    val f = rootProject.file("keys.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
fun key(name: String): String = (keys.getProperty(name) ?: "")

android {
    namespace = "com.loadcast.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.loadcast.app"
        minSdk = 26 // core가 java.time.LocalDate 사용 — 26 미만 지원 시 desugaring 필요
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"

        buildConfigField("String", "SUPABASE_URL", "\"${key("SUPABASE_URL")}\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"${key("SUPABASE_ANON_KEY")}\"")
        buildConfigField("String", "KAKAO_NATIVE_APP_KEY", "\"${key("KAKAO_NATIVE_APP_KEY")}\"")
        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"${key("GOOGLE_WEB_CLIENT_ID")}\"")
        buildConfigField("String", "NAVER_CLIENT_ID", "\"${key("NAVER_CLIENT_ID")}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(project(":core"))

    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.navigation:navigation-compose:2.8.5")
}
