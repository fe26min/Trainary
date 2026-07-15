# 인증 설계 & 설정 가이드

백엔드는 **Supabase**(Auth + Postgres)를 사용합니다. 이메일/비밀번호에 더해
소셜 로그인은 **카카오 · Google · 네이버**를 제공하고, **iOS는 Apple 로그인 필수**입니다.

> ⚠️ **Apple 심사 규정(4.8 Login Services)**: iOS 앱이 서드파티 소셜 로그인(카카오·Google·네이버)을
> 제공하면 **Sign in with Apple을 함께 제공해야** 심사를 통과합니다. iOS 출시 요건으로 취급하세요.
> Android에는 Apple 로그인이 필요 없습니다.

## 우선순위

| 단계 | 항목 | 방식 |
|---|---|---|
| 1 | 이메일/비밀번호 (가입·로그인·재설정 메일) | Supabase Auth 기본 |
| 2 | 카카오 | Supabase 기본 제공 OAuth 프로바이더 |
| 3 | Google | Supabase 기본 제공 (네이티브 ID 토큰 교환 권장) |
| 4 | Apple (iOS만) | Supabase 기본 제공 (`ASAuthorizationController` → ID 토큰 교환) |
| 5 | 네이버 | Supabase **기본 제공 아님** → 커스텀 (아래 참고) |

## Supabase 프로젝트 설정

1. https://supabase.com 에서 프로젝트 생성 → `Project URL`과 `anon key` 확보
2. Authentication → Providers에서 Email, Kakao, Google, Apple 활성화
3. Authentication → URL Configuration에 앱 딥링크 등록: `com.loadcast.app://auth-callback`
4. 클라이언트 SDK
   - Android: [`supabase-kt`](https://github.com/supabase-community/supabase-kt) (`auth-kt`)
   - iOS: [`supabase-swift`](https://github.com/supabase/supabase-swift)
5. 키 보관: 코드에 하드코딩하지 말 것.
   - Android: `local.properties` → `BuildConfig` 주입
   - iOS: `Config.xcconfig`(gitignore) → Info.plist 주입

### 데이터베이스 스키마 (초안)

```sql
create table public.profiles (
  id uuid primary key references auth.users on delete cascade,
  nickname text not null,
  goal text not null,              -- 운동 목적 (단일)
  sports text[] not null,          -- 주 종목 (다중)
  weekly_frequency text not null,  -- '1_2' | '3_4' | '5_PLUS'
  experience text not null,        -- 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED'
  custom_types text[] not null default '{}',
  created_at timestamptz not null default now()
);

create table public.workouts (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users on delete cascade,
  date date not null,
  type text not null,              -- WorkoutType enum 문자열
  custom_type_label text,
  duration_min int not null check (duration_min > 0),
  intensity text not null,         -- 'LOW' | 'MODERATE' | 'HIGH'
  satisfaction int check (satisfaction between 1 and 5),
  memo text,
  created_at timestamptz not null default now()
);
create index workouts_user_date on public.workouts (user_id, date desc);

-- RLS: 본인 데이터만 접근
alter table public.profiles enable row level security;
alter table public.workouts enable row level security;
create policy "own profile" on public.profiles for all using (auth.uid() = id) with check (auth.uid() = id);
create policy "own workouts" on public.workouts for all using (auth.uid() = user_id) with check (auth.uid() = user_id);
```

`load`(부하) 컬럼은 만들지 않습니다 — 항상 클라이언트 코어 로직으로 계산 (docs/SPEC.md §1).

## 프로바이더별 메모

### 카카오
- [Kakao Developers](https://developers.kakao.com)에서 앱 생성 → REST API 키·Client Secret 발급
- Supabase Kakao 프로바이더에 키 입력, Redirect URI는 Supabase가 제공하는 값을 카카오 콘솔에 등록
- 모바일에서는 `signInWithOAuth(provider = Kakao)` → 인앱 브라우저 → 딥링크 복귀

### Google
- 웹 방식(OAuth 리다이렉트)도 되지만, 네이티브 UX를 위해
  **Credential Manager(Android) / GoogleSignIn(iOS)** 로 ID 토큰을 받은 뒤
  `signInWithIdToken(provider = Google, idToken = ...)` 교환 방식을 권장

### Apple (iOS 전용, 출시 필수)
- `Sign in with Apple` capability 추가 → `ASAuthorizationController`로 ID 토큰 획득
- `signInWithIdToken(provider = Apple, idToken = ...)` 교환

### 네이버 (커스텀 — 마지막에 구현)
Supabase 기본 프로바이더가 아니므로 다음 흐름을 사용합니다:
1. 네이버 로그인 SDK(iOS/Android)로 네이버 access token 획득
2. Supabase **Edge Function**으로 토큰 전달 → 네이버 프로필 API로 검증
3. Edge Function이 `service_role` 키로 사용자 upsert 후 세션 발급(admin `generateLink` 또는 custom JWT)
4. 공수가 큰 편이므로 MVP 검증 후 도입 여부 재판단 권장

## 세션 & 화면 연결 (디자인 문서 매핑)

| 디자인 화면 | 구현 |
|---|---|
| 01 스플래시 | 저장된 세션 토큰 확인만 (SDK가 자동 갱신) → 홈 또는 로그인 분기 |
| 02 로그인 | `signInWith(Email)` + 소셜 버튼 4종(iOS)/3종(Android) |
| 03 회원가입 | `signUpWith(Email)` — 이메일 형식·중복·비밀번호 불일치 인라인 에러 |
| 04 비밀번호 재설정 | `resetPasswordForEmail(email)` → 발송 완료 상태 |
| 15 로그아웃 모달 | `signOut()` — 로컬 기록 캐시는 유지, 토큰만 폐기 |
| 16 계정 탈퇴 모달 | Edge Function에서 `auth.admin.deleteUser()` (클라이언트에서 직접 불가) — 서버 데이터 즉시 삭제 |
