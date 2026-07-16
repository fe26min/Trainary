import SwiftUI
import LoadcastCore

/// 운동 기록 추가/수정 폼 — 디자인 화면 06·07.
/// 필수 4개(날짜·종류·시간·강도)만 검증. 부하 = 시간 × 강도계수 자동 계산.
/// 삭제는 하단 약한 텍스트 버튼 → 확인 모달(디자인 화면 14).
struct RecordFormView: View {
    @EnvironmentObject var store: RecordStore
    @Environment(\.dismiss) private var dismiss

    let existingID: String?

    @State private var type: WorkoutType = .crossfit
    @State private var customLabel = ""
    @State private var duration = 60
    @State private var intensity: Intensity = .moderate
    @State private var satisfaction: Int?
    @State private var memo = ""
    @State private var showDeleteConfirm = false
    @State private var loaded = false

    private let today = Today.value()

    private var existing: WorkoutRecord? { existingID.flatMap(store.find) }
    private var isEdit: Bool { existing != nil }
    private var estimatedLoad: Int { LoadCalculator.sessionLoad(durationMin: duration, intensity: intensity) }
    private var date: DayDate { existing?.date ?? today }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 18) {
                FieldLabel("운동 날짜")
                readonlyField(Strings.formDate(date))

                FieldLabel("운동 종류")
                typeSelector
                if type == .other {
                    TextField("예: 클라이밍, 복싱", text: $customLabel)
                        .textFieldStyle(.roundedBorder)
                }

                FieldLabel("운동 시간")
                durationStepper

                FieldLabel("운동 강도")
                intensitySelector

                estimatedLoadRow

                FieldLabel("운동 만족도 (선택)")
                satisfactionSelector

                FieldLabel("메모 (선택)")
                TextField("운동 내용이나 오늘 컨디션을 간단히 기록해보세요.", text: $memo, axis: .vertical)
                    .lineLimit(3, reservesSpace: true)
                    .textFieldStyle(.roundedBorder)

                PrimaryButton(title: isEdit ? "변경 내용 저장" : "운동 기록 저장", action: save)

                if isEdit {
                    Button(role: .destructive) { showDeleteConfirm = true } label: {
                        Label("운동 기록 삭제", systemImage: "trash")
                            .font(.subheadline).foregroundStyle(LoadcastColors.neutral600)
                            .frame(maxWidth: .infinity)
                    }
                }
            }
            .padding(18)
        }
        .background(LoadcastColors.bg)
        .navigationTitle(isEdit ? "운동 기록 수정" : "운동 기록 추가")
        .navigationBarTitleDisplayMode(.inline)
        .onAppear(perform: loadExisting)
        .alert("운동 기록을 삭제할까요?", isPresented: $showDeleteConfirm) {
            Button("취소", role: .cancel) {}
            Button("삭제", role: .destructive) {
                if let id = existingID { store.delete(id) }
                dismiss()
            }
        } message: {
            Text("삭제한 기록은 되돌릴 수 없으며, 최근 7일 분석에서도 제외됩니다.")
        }
    }

    private func loadExisting() {
        guard !loaded, let e = existing else { loaded = true; return }
        type = e.type
        customLabel = e.customTypeLabel ?? ""
        duration = e.durationMin
        intensity = e.intensity
        satisfaction = e.satisfaction
        memo = e.memo ?? ""
        loaded = true
    }

    private func save() {
        let record = WorkoutRecord(
            date: date,
            type: type,
            durationMin: duration,
            intensity: intensity,
            id: existing?.id ?? UUID().uuidString,
            customTypeLabel: (type == .other && !customLabel.isEmpty) ? customLabel : nil,
            satisfaction: satisfaction,
            memo: memo.isEmpty ? nil : memo
        )
        store.upsert(record)
        dismiss()
    }

    private func readonlyField(_ text: String) -> some View {
        Text(text)
            .frame(maxWidth: .infinity, alignment: .leading)
            .frame(height: 48).padding(.horizontal, 14)
            .background(LoadcastColors.surface)
            .overlay(Rectangle().stroke(LoadcastColors.divider, lineWidth: 1))
    }

    private var typeSelector: some View {
        LazyVGrid(columns: Array(repeating: GridItem(.flexible(), spacing: 7), count: 4), spacing: 7) {
            ForEach(WorkoutType.allCases, id: \.self) { t in
                Chip(text: Strings.typeLabel(t), selected: t == type) { type = t }
            }
        }
    }

    private var intensitySelector: some View {
        HStack(spacing: 8) {
            ForEach(Intensity.allCases, id: \.self) { level in
                let isSel = level == intensity
                let palette = Palette.of(level)
                VStack(alignment: .leading, spacing: 4) {
                    HStack {
                        Text(Strings.intensityLabel(level)).font(.subheadline.weight(.semibold))
                            .foregroundStyle(isSel ? palette.onTint : LoadcastColors.neutral700)
                        Spacer()
                        if isSel { Image(systemName: "checkmark").font(.caption).foregroundStyle(palette.onTint) }
                    }
                    Text(Strings.intensityDescription(level)).font(.caption2)
                        .foregroundStyle(isSel ? palette.onTint : LoadcastColors.neutral500)
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                .frame(height: 80).padding(10)
                .background(isSel ? palette.tint : LoadcastColors.surface)
                .overlay(Rectangle().stroke(isSel ? palette.main : LoadcastColors.divider, lineWidth: isSel ? 2 : 1))
                .onTapGesture { intensity = level }
            }
        }
    }

    private var estimatedLoadRow: some View {
        HStack {
            Text("예상 운동 부하").foregroundStyle(LoadcastColors.accent800)
            Spacer()
            Text("\(estimatedLoad) 점").font(.title3.weight(.semibold)).foregroundStyle(LoadcastColors.accent800)
        }
        .padding(.horizontal, 15).padding(.vertical, 13)
        .background(LoadcastColors.accent100)
        .overlay(Rectangle().stroke(LoadcastColors.accent300, lineWidth: 1))
    }

    private var satisfactionSelector: some View {
        HStack(spacing: 8) {
            ForEach(1...5, id: \.self) { n in
                let isSel = satisfaction == n
                Text("\(n)")
                    .foregroundStyle(isSel ? LoadcastColors.surface : LoadcastColors.neutral500)
                    .frame(maxWidth: .infinity).frame(height: 44)
                    .background(isSel ? LoadcastColors.accent : LoadcastColors.surface)
                    .overlay(Rectangle().stroke(isSel ? LoadcastColors.accent : LoadcastColors.divider, lineWidth: isSel ? 2 : 1))
                    .onTapGesture { satisfaction = n }
            }
        }
    }

    private var durationStepper: some View {
        HStack(spacing: 10) {
            stepperButton("−") { duration = max(1, duration - 5) }
            Text("\(duration) 분").font(.title2.weight(.semibold)).foregroundStyle(LoadcastColors.text)
                .frame(maxWidth: .infinity).frame(height: 46)
                .background(LoadcastColors.surface)
                .overlay(Rectangle().stroke(LoadcastColors.divider, lineWidth: 1))
            stepperButton("＋") { duration += 5 }
        }
    }

    private func stepperButton(_ symbol: String, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Text(symbol).font(.title2).foregroundStyle(LoadcastColors.text)
                .frame(width: 46, height: 46)
                .background(LoadcastColors.surface)
                .overlay(Rectangle().stroke(LoadcastColors.divider, lineWidth: 1))
        }.buttonStyle(.plain)
    }
}
