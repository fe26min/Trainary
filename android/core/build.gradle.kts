plugins {
    id("org.jetbrains.kotlin.jvm")
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    testImplementation("junit:junit:4.13.2")
    testImplementation("com.google.code.gson:gson:2.11.0")
}

tasks.test {
    // 공유 테스트 벡터(spec/test-vectors.json) 경로를 테스트에 전달
    systemProperty(
        "loadcast.vectors.path",
        rootProject.projectDir.resolve("../spec/test-vectors.json").absolutePath
    )
}
