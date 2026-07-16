import SwiftUI
import LoadcastCore

/// 운동 기록 목록 — 디자인 화면 05.
/// 필터 칩(클라이언트 필터) · 카드 전체가 수정 진입 영역 · 빈 상태.
struct RecordsView: View {
    @EnvironmentObject var store: RecordStore
    @State private var selectedFilter = 0

    private var filterTypes: [WorkoutType?] {
        [nil] + WorkoutType.allCases.filter { t in store.records.contains { $0.type == t } }
    }

    private var visible: [WorkoutRecord] {
        let types = filterTypes
        let idx = min(selectedFilter, types.count - 1)
        guard let type = types[idx] else { return store.records }
        return store.records.filter { $0.type == type }
    }

    var body: some View {
        VStack(spacing: 0) {
            HStack {
                Text("운동 기록").font(.title2.weight(.semibold)).foregroundStyle(LoadcastColors.text)
                Spacer()
                NavigationLink(value: Route.add) {
                    Image(systemName: "plus").foregroundStyle(LoadcastColors.surface)
                        .frame(width: 38, height: 38).background(LoadcastColors.accent)
                }.buttonStyle(.plain)
            }
            .padding(.horizontal, 18).padding(.top, 12).padding(.bottom, 8)

            if store.records.isEmpty {
                emptyState
            } else {
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 7) {
                        ForEach(Array(filterTypes.enumerated()), id: \.offset) { index, type in
                            Chip(text: type.map(Strings.typeLabel) ?? "전체", selected: index == selectedFilter) {
                                selectedFilter = index
                            }
                        }
                    }
                    .padding(.horizontal, 18).padding(.vertical, 4)
                }

                ScrollView {
                    LazyVStack(spacing: 10) {
                        ForEach(visible) { record in
                            NavigationLink(value: Route.edit(record.id)) { recordCard(record) }
                                .buttonStyle(.plain)
                        }
                    }
                    .padding(.horizontal, 18).padding(.vertical, 10)
                }
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(LoadcastColors.bg)
        .loadcastDestinations()
    }

    private func recordCard(_ record: WorkoutRecord) -> some View {
        VStack(alignment: .leading, spacing: 6) {
            HStack {
                Text(Strings.recordTypeLabel(record)).font(.headline).foregroundStyle(LoadcastColors.text)
                Spacer()
                StatusBadge(text: "\(Strings.intensityLabel(record.intensity)) 강도", palette: Palette.of(record.intensity))
            }
            Text("\(Strings.shortDate(record.date)) · \(record.durationMin)분 · 부하 \(record.load)점")
                .font(.caption).foregroundStyle(LoadcastColors.neutral600)
            if let memo = record.memo, !memo.isEmpty {
                Text("“\(memo)”").font(.caption).foregroundStyle(LoadcastColors.neutral500).lineLimit(1)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(15)
        .background(LoadcastColors.surface)
        .overlay(Rectangle().stroke(LoadcastColors.divider, lineWidth: 1))
    }

    private var emptyState: some View {
        VStack(spacing: 14) {
            Spacer()
            Image(systemName: "tray").font(.system(size: 30)).foregroundStyle(LoadcastColors.neutral500)
                .frame(width: 64, height: 64).background(LoadcastColors.neutral100)
            Text("아직 운동 기록이 없습니다.").font(.headline).foregroundStyle(LoadcastColors.text)
            Text("첫 운동을 기록하고 오늘의 추천을 받아보세요.").font(.subheadline).foregroundStyle(LoadcastColors.neutral600)
            NavigationLink(value: Route.add) {
                Label("운동 기록 추가", systemImage: "plus")
                    .font(.subheadline).foregroundStyle(LoadcastColors.surface)
                    .padding(.horizontal, 22).frame(height: 48).background(LoadcastColors.accent)
            }.buttonStyle(.plain)
            Spacer()
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .padding(34)
    }
}
