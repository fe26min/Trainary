# LOADCAST (Trainary)

최근 7일 운동 부하와 피로도를 계산해 **"오늘 운동을 해도 되는가 · 어느 강도로 해야 하는가"** 에
1분 안에 답하는 모바일 앱. iOS(SwiftUI)와 Android(Jetpack Compose) **순수 네이티브 2개 앱**으로 개발합니다.

- 디자인: 클로드 디자인 "운동 부하 분석 앱 설계" v1.0 (화면 19종 · 컴포넌트 17종)
- 개발 명세(단일 기준): [`docs/SPEC.md`](docs/SPEC.md)
- 인증(이메일 + 카카오·Google·네이버, iOS는 Apple 필수): [`docs/AUTH.md`](docs/AUTH.md)

## 저장소 구조

```
├── docs/
│   ├── SPEC.md              # 계산 로직·데이터 모델 명세 (두 앱의 단일 기준)
│   └── AUTH.md              # Supabase + 소셜 로그인 설정 가이드
├── spec/
│   └── test-vectors.json    # 공유 테스트 벡터 — 두 플랫폼 테스트가 모두 이 파일을 읽음
├── android/                 # Android (Kotlin · Jetpack Compose)
│   ├── core/                # 순수 Kotlin 계산 로직 + 벡터 테스트 (Android SDK 불필요)
│   └── app/                 # Compose 앱 (Android SDK 필요)
└── ios/                     # iOS (Swift · SwiftUI)
    ├── LoadcastCore/        # Swift Package 계산 로직 + 벡터 테스트
    └── Loadcast/            # SwiftUI 앱 (XcodeGen project.yml 포함)
```

## 핵심 원칙: 로직 동등성

부하·ACWR·추천 판정 로직은 Kotlin(`android/core`)과 Swift(`ios/LoadcastCore`)에 **각각 구현**되어 있고,
`spec/test-vectors.json` 하나를 양쪽 테스트가 읽어 **동일한 결과**를 보장합니다.
로직을 바꿀 때는 반드시 ① `docs/SPEC.md` 수정 → ② 벡터 추가/수정 → ③ 두 구현 모두 수정 순서로 진행하세요.

## 빌드 & 테스트

### Android

```bash
cd android
gradle wrapper --gradle-version 8.14.3   # 최초 1회: 래퍼 생성 (Gradle 8.x 설치 필요)
./gradlew :core:test          # 계산 로직 테스트 (JDK 17+만 있으면 어디서든 실행 가능)
./gradlew :app:assembleDebug  # 앱 빌드 (Android SDK 필요 — Android Studio 권장)
```

> 래퍼 바이너리(gradle-wrapper.jar)는 저장소에 포함하지 않았습니다. Android Studio로 열면
> 자동으로 처리되고, CLI에서는 위 `gradle wrapper` 한 번이면 됩니다.

`app` 모듈은 Android SDK가 감지될 때만(`ANDROID_HOME` 또는 `android/local.properties`) 빌드에 포함됩니다.
따라서 SDK 없는 CI/환경에서도 `:core:test`는 항상 돌릴 수 있습니다.

### iOS (macOS 필요)

```bash
cd ios/LoadcastCore && swift test   # 계산 로직 테스트
cd ios && xcodegen generate         # Loadcast.xcodeproj 생성 (brew install xcodegen)
open Loadcast.xcodeproj
```

## 현재 상태 (스캐폴딩 단계)

| 항목 | 상태 |
|---|---|
| 계산 로직 명세 + 공유 테스트 벡터 | ✅ |
| Kotlin 코어 로직 + 테스트 | ✅ (이 저장소 CI 환경에서 실행 검증) |
| Swift 코어 로직 + 테스트 | ✅ 작성 완료 — macOS에서 `swift test` 실행 필요 |
| Android 앱 스켈레톤 (토큰·4탭·화면 스텁) | ✅ 작성 완료 — Android Studio에서 빌드 확인 필요 |
| iOS 앱 스켈레톤 (토큰·4탭·화면 스텁) | ✅ 작성 완료 — Xcode에서 빌드 확인 필요 |
| Supabase 연동 · 소셜 로그인 | 📋 가이드만 작성 (docs/AUTH.md) — 구현 예정 |
| 기록 CRUD · 홈 · 분석 · 추천 화면 | 📋 예정 |

> ⚠️ 이 스캐폴딩은 리눅스 환경에서 작성되어 **Kotlin 코어 테스트만 실행 검증**되었습니다.
> 앱 모듈(iOS 전체, Android app)은 각 플랫폼 IDE에서 첫 빌드 확인이 필요합니다.

## 일정 참고

순수 네이티브 2개 앱은 같은 화면을 두 번 만드는 방식이라, 1인 개발 기준 플랫폼당 6~8주
(합계 약 3~4개월)를 권장합니다. 기능은 `docs/SPEC.md`를 기준으로 두 앱이 항상 동등하게 유지합니다.
