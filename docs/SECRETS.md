# 키 · 시크릿 관리

**어떤 키도 저장소에 커밋하지 않습니다.** 실제 키는 gitignore된 파일에 두고, 저장소에는 빈
`*.example` 템플릿만 올립니다. 새 팀원/기기는 템플릿을 복사해 값을 채웁니다.

## 민감도 3단계

| 등급 | 예시 | 어디에 두나 |
|---|---|---|
| 공개 가능 | Supabase **anon key**, Supabase URL | 클라이언트에 심어도 됨(보안은 RLS가 담당). 그래도 위생·환경분리 위해 config 파일로 관리 |
| 클라이언트 비공개 | 카카오 네이티브 앱 키, Google OAuth client ID, 네이버 client ID | gitignore된 `keys.properties` / `Secrets.xcconfig` |
| 서버 전용 비밀 | 카카오 **Client Secret**, Supabase **service_role** 키 | **저장소에 절대 없음.** Supabase 대시보드 / Edge Function secrets 에만 |

> 네이버 로그인은 Supabase 기본 프로바이더가 아니라 Edge Function으로 토큰을 검증합니다(docs/AUTH.md).
> 이때 필요한 네이버 client secret 등은 `supabase secrets set` 으로 서버에만 저장합니다.

## Android

1. `cd android && cp keys.properties.example keys.properties` (keys.properties 는 .gitignore 대상)
2. 값 입력. 파일이 없거나 비어도 **빌드는 성공**합니다(빈 문자열 fallback, 소셜 로그인만 비활성).
3. `app/build.gradle.kts`가 이 파일을 읽어 `BuildConfig.SUPABASE_URL` 등으로 주입 → 코드에서 `BuildConfig.*`로 사용.

```
keys.properties  (gitignore, 실제 값)
keys.properties.example  (커밋, 빈 템플릿)
```

## iOS

1. `cd ios && cp Secrets.example.xcconfig Secrets.xcconfig` (Secrets.xcconfig 는 .gitignore 대상)
2. 값 입력. 파일이 없어도 빌드 성공: 커밋된 `Config.xcconfig`가 기본 빈 값을 정의하고
   `#include? "Secrets.xcconfig"`(물음표 = 없어도 에러 없음)로 값을 덮어씁니다.
3. `Config.xcconfig` → `Loadcast/Info.plist`의 `$(SUPABASE_HOST)` 등 → 코드에서 `Bundle.main`으로 읽음
   (`AppSecrets.swift` 참고).

```
Config.xcconfig          (커밋, 기본 빈 값 + optional include)
Secrets.xcconfig         (gitignore, 실제 값)
Secrets.example.xcconfig (커밋, 빈 템플릿)
Loadcast/Info.plist      (커밋, $(...) 플레이스홀더만 — 값 없음)
```

## 이미 .gitignore로 막아둔 것

`android/keys.properties`, `ios/Secrets.xcconfig`, `google-services.json`,
`GoogleService-Info.plist`, `.env`, `*.keystore`, `*.jks`.

## 실수로 커밋했다면

키를 즉시 **폐기·재발급**하세요(히스토리에서 지워도 이미 노출된 것으로 간주). 그 후
`git rm --cached`로 추적 해제 + .gitignore 확인. 히스토리 세탁이 필요하면 `git filter-repo` 사용.
