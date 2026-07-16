import Foundation
import LoadcastCore

/// 기록 로컬 저장소 — Documents 디렉터리의 JSON 파일 기반.
/// MVP 단계: 파일 하나 + 동기 IO. TODO: Supabase 연동 시 원격 동기화 캐시로 전환 (docs/AUTH.md).
@MainActor
final class RecordStore: ObservableObject {

    /// 항상 날짜 내림차순 정렬 유지.
    @Published private(set) var records: [WorkoutRecord] = []

    private let fileURL: URL

    init(fileURL: URL? = nil) {
        self.fileURL = fileURL ?? Self.defaultURL()
        load()
    }

    private static func defaultURL() -> URL {
        let dir = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)[0]
        return dir.appendingPathComponent("records.json")
    }

    private func load() {
        let data = (try? Data(contentsOf: fileURL)) ?? Data()
        records = RecordCodec.fromJson(data).sorted { $0.date > $1.date }
    }

    func find(_ id: String) -> WorkoutRecord? {
        records.first { $0.id == id }
    }

    /// 가입일 대용 — 가장 이른 기록 날짜 (없으면 오늘). TODO: 인증 연동 후 계정 생성일 사용.
    func firstUseDate(today: DayDate) -> DayDate {
        records.map(\.date).min() ?? today
    }

    func upsert(_ record: WorkoutRecord) {
        records.removeAll { $0.id == record.id }
        records.append(record)
        records.sort { $0.date > $1.date }
        persist()
    }

    func delete(_ id: String) {
        records.removeAll { $0.id == id }
        persist()
    }

    private func persist() {
        guard let data = try? RecordCodec.toJson(records) else { return }
        try? data.write(to: fileURL, options: .atomic)
    }
}
