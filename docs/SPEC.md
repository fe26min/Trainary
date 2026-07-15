# LOADCAST 개발 명세 (v1.0)

디자인 문서 "운동 부하 분석 앱 설계" v1.0(2026.07)의 **계산 로직 명세(섹션 10)** 를 코드로 옮기기 위한
단일 기준 문서입니다. iOS(Swift)와 Android(Kotlin)는 반드시 이 문서와 동일하게 동작해야 하며,
두 구현의 일치는 `spec/test-vectors.json` 공유 테스트 벡터로 검증합니다.

> 모든 임계값은 초기 기본값이며 운영 데이터로 보정합니다(디자인 문서와 동일한 원칙).

---

## 1. 데이터 모델

### WorkoutRecord (운동 기록)

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| `id` | UUID | ✓ | |
| `date` | 날짜(시간 없음, 기기 로컬 타임존) | ✓ | 운동한 날 |
| `type` | WorkoutType | ✓ | 아래 enum |
| `customTypeLabel` | String? | | `type == OTHER`이고 사용자가 직접 추가한 종류일 때 표시명 |
| `durationMin` | Int (1 이상) | ✓ | 운동 시간(분) |
| `intensity` | Intensity | ✓ | LOW / MODERATE / HIGH |
| `satisfaction` | Int? (1~5) | | 선택 |
| `memo` | String? | | 선택 |

`load`(부하)는 **저장하지 않고 항상 계산**합니다(공식 변경 시 과거 데이터 재계산 문제 방지).

### WorkoutType

`CROSSFIT, RUNNING, WEIGHT, YOGA, SWIMMING, CYCLING, HIKING, OTHER`

사용자 정의 종류는 `OTHER` + `customTypeLabel`로 저장하고, 목록 필터 칩에 자동 추가합니다.

### Profile (온보딩)

`nickname, goal(단일), sports(다중), weeklyFrequency(1_2 | 3_4 | 5_PLUS), experience(BEGINNER | INTERMEDIATE | ADVANCED)`

---

## 2. 부하 공식 (Foster sRPE)

```
부하(점) = durationMin × 강도 계수
```

| 강도 | 계수 | RPE 기준 |
|---|---|---|
| LOW (낮음) | ×1 | RPE 1~3 · 대화 가능 |
| MODERATE (중간) | ×2 | RPE 4~6 · 땀나고 집중 필요 |
| HIGH (높음) | ×3 | RPE 7~10 · 숨차고 회복 부담 큼 |

종목별 가중치는 없습니다. 예: 60분 HIGH = 180점, 55분 MODERATE = 110점, 35분 LOW = 35점.

---

## 3. 윈도우 정의 (전부 "오늘 포함 롤링", 달력 주 아님)

- **급성(acute) 윈도우**: `[today-6, today]` — 7일
- **만성(chronic) 윈도우**: `[today-27, today]` — 28일
- **최근 3일**: `[today-2, today]`
- **휴식일(restDays7)**: `[today-6, today-1]`(과거 6일) 중 기록이 0건인 날의 수.
  오늘은 아직 끝나지 않았으므로 휴식일 판정에서 제외. 미래 날짜는 존재하지 않음.

## 4. ACWR

```
acuteLoad   = 급성 윈도우 부하 합
chronicLoad = 만성 윈도우 부하 합
ACWR        = acuteLoad ÷ (chronicLoad ÷ 4)
```

- `chronicLoad == 0`이면 ACWR은 **미정의(null)**.
- **acwrAvailable** = `firstUseDate ≤ today - 27` **AND** `chronicLoad > 0`.
  (가입/최초 기록일로부터 28일이 지나기 전에는 만성 부하가 통계적으로 무의미하므로 ACWR 사용 금지 → 추천은 "초기 추정" 모드)

### 분석 탭 부하 상태 4단계 (LoadStatus)

경계 포함 규칙까지 고정:

| 상태 | 조건 |
|---|---|
| `LOW` (낮음) | ACWR < 0.8 |
| `OPTIMAL` (적정) | 0.8 ≤ ACWR ≤ 1.3 |
| `CAUTION` (주의) | 1.3 < ACWR ≤ 1.5 |
| `OVERLOAD` (과부하) | ACWR > 1.5 |
| `UNKNOWN` | ACWR 미정의(만성 부하 0 또는 데이터 부족) |

---

## 5. 오늘 운동 추천 (3단계)

### 입력

`today`, 전체 `records`, `firstUseDate`(계정 생성일; 없으면 가장 이른 기록 날짜).

### 콜드 스타트

1. 전체 기록 수 `< 3` → 결과는 `InsufficientData(recordedCount, requiredCount = 3)`.
   (홈 히어로를 "데이터 부족" 카드로 대체 — 디자인 화면 19)
2. 기록 3건 이상이지만 `acwrAvailable == false` → ACWR 조건을 **건너뛰고** 단순 규칙만 평가,
   결과에 `estimate = true` ("초기 추정" 배지).

### 판정 규칙 — 위에서부터 평가, 첫 매치 승 (디자인 문서와 동일)

| 순서 | 상태 | 조건 (OR) | reason code |
|---|---|---|---|
| 1 | `RECOVERY` (회복 권장) | acwrAvailable AND ACWR > 1.5 | `ACWR_HIGH` |
| | | 어제 HIGH AND 최근 3일 HIGH ≥ 2회 | `CONSECUTIVE_HIGH` |
| | | restDays7 == 0 | `NO_REST` |
| 2 | `MODERATE` (중강도 권장) | acwrAvailable AND 1.3 < ACWR ≤ 1.5 | `ACWR_ELEVATED` |
| | | 어제 HIGH 1회 | `YESTERDAY_HIGH` |
| | | 급성 윈도우 HIGH ≥ 2회 | `FREQUENT_HIGH` |
| 3 | `HIGH_OK` (고강도 가능) | 위 모두 해당 없음 | — |
| | | (보조) acwrAvailable AND ACWR < 0.8 | `ACWR_LOW_ROOM` ("운동량을 조금 늘려도 좋아요") |

- `reasons`: **선택된 상태의 매치된 조건 코드만**, 표 순서대로, **최대 3개**.
- 사용자 노출 문구는 앱 레이어에서 reason code → 한국어 문자열로 매핑(코어는 코드만 반환).
- 추천 문구에 단정적 의학 표현 금지(디자인 문서 개발 주의).

## 6. 주간 요약 (분석 탭 상단 4지표)

급성 윈도우 기준: `totalMinutes`, `totalLoad`, `highCount`(HIGH 기록 수), `restDays`(§3 정의).
이전 기간 비교는 `[today-13, today-7]` vs 급성 윈도우. 이전 기간 기록 0건이면
"비교할 이전 기록이 아직 없어요" 표시(증감 계산 안 함).

## 7. 부위별 피로 추정 (홈 카드)

종목 → 부위 그룹 매핑:

| 종목 | LOWER(하체) | UPPER(상체) | CORE(코어) | CARDIO(심폐) |
|---|---|---|---|---|
| CROSSFIT | ✓ | ✓ | | ✓ |
| RUNNING | ✓ | | | ✓ |
| WEIGHT | ✓ | ✓ | ✓ | |
| YOGA | | | ✓ | |
| SWIMMING | | ✓ | | ✓ |
| CYCLING | ✓ | | | ✓ |
| HIKING | ✓ | | | ✓ |
| OTHER(및 사용자 정의) | | | | ✓ |

그룹 피로 = **최근 3일** 해당 그룹에 매핑되는 기록들의 부하 합 `L`:
`L < 100 → LOW(낮음)`, `100 ≤ L < 250 → MODERATE(보통)`, `L ≥ 250 → HIGH(높음)`.

홈 카드에는 항상 "추정" 배지 + 안내 문구를 표시합니다(디자인 문서와 동일).

## 8. 구현 규칙

- 코어 로직은 **순수 함수**: 시스템 시계·타임존·랜덤·I/O 접근 금지. `today`는 항상 인자로 받는다.
- 부동소수 비교: ACWR 경계 판정은 `Double` 그대로 비교(벡터가 경계값을 직접 검증).
- 두 플랫폼 모두 `spec/test-vectors.json`을 읽는 테스트가 전부 통과해야 배포 가능.
