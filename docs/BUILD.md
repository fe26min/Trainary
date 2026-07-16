# 로컬 빌드 가이드

> 이 저장소는 리눅스 CI에서 작성돼 **계산 코어(JVM)만 실행 검증**되었습니다.
> 앱 UI는 각 플랫폼 IDE에서 첫 빌드가 필요합니다. 에러가 나오면 그대로 공유해 주세요.

## 사전 준비 (선택 — 키 없이도 빌드는 됩니다)

- Android: `cd android && cp keys.properties.example keys.properties`
- iOS: `cd ios && cp Secrets.example.xcconfig Secrets.xcconfig`

소셜 로그인은 아직 구현 전이라 값을 비워둬도 홈/기록/분석/추천 화면은 모두 동작합니다. (docs/SECRETS.md)

## Android

요구: Android Studio (Ladybug 이상) 또는 JDK 17 + Android SDK 35.

```bash
cd android
./gradlew :core:test          # 계산 로직 테스트 (SDK 불필요) — 먼저 여기서 초록 확인
# 앱 빌드/실행은 Android Studio로 android/ 폴더 열기 → 에뮬레이터(API 26+)에서 Run
./gradlew :app:assembleDebug  # 또는 CLI 빌드 (Android SDK 필요)
```

> ⚠️ 시스템에 설치된 `gradle`을 직접 쓰지 마세요(버전이 낮으면
> "FoojayToolchainsPlugin needs Gradle 7.6+" 같은 에러). 반드시 프로젝트의 `./gradlew`를 사용합니다 —
> 올바른 Gradle 8.14.3을 자동으로 내려받습니다. Android Studio로 열면 자동으로 래퍼를 씁니다.

첫 빌드 시 Gradle이 AGP·Compose 의존성을 내려받습니다(네트워크 필요).

## iOS

요구: macOS + Xcode 15+, [XcodeGen](https://github.com/yonaskolb/XcodeGen)(`brew install xcodegen`).

```bash
cd ios/LoadcastCore && swift test   # 계산 로직 테스트 — 먼저 여기서 초록 확인
cd .. && xcodegen generate           # Loadcast.xcodeproj 생성
open Loadcast.xcodeproj               # Xcode에서 시뮬레이터 선택 후 Run (⌘R)
```

## 빌드가 되면 이렇게 확인해 주세요

1. **기록 추가**: 기록 탭 → ＋ → 종류·시간·강도 입력 → 저장. 목록에 뜨는지.
2. **3건 이상** 넣으면 홈 히어로가 "데이터 부족"에서 **추천 카드**로 바뀌는지.
3. **홈**: 현재 상태 요약·부위별 피로 막대·최근 7일·최근 운동이 그려지는지.
4. **분석 탭**: 4지표·부하 상태·요일별 막대·이전 기간 비교.
5. **추천 상세**: 홈 히어로 "추천 상세 보기" → 상태·이유·추천 운동.
6. **수정/삭제**: 기록 카드 탭 → 값 변경 저장 / 하단 삭제 → 확인 모달.

## 문제가 생기면

- **컴파일 에러**: 파일명·라인·메시지를 그대로 붙여 주세요. 바로 고치겠습니다.
- **화면이 이상함**: 스크린샷을 주시면 그걸 기준으로 UI를 다듬겠습니다.
- 색·간격·폰트 등 디자인 토큰은 `DesignTokens.swift`(iOS) / `ui/theme/Color.kt`(Android) 한 곳에서 관리됩니다.
