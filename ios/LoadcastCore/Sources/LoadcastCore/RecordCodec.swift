import Foundation

/// 기록 목록 ↔ JSON 직렬화 (로컬 파일 저장용).
/// Android(core RecordCodec)와 같은 포맷: 날짜는 "yyyy-MM-dd" 문자열, enum은 이름 문자열, null 필드 생략.
public enum RecordCodec {

    private struct DTO: Codable {
        let id: String
        let date: String
        let type: String
        let durationMin: Int
        let intensity: String
        let customTypeLabel: String?
        let satisfaction: Int?
        let memo: String?
    }

    public static func toJson(_ records: [WorkoutRecord]) throws -> Data {
        let dtos = records.map { r in
            DTO(
                id: r.id,
                date: r.date.isoString,
                type: r.type.rawValue,
                durationMin: r.durationMin,
                intensity: r.intensity.rawValue,
                customTypeLabel: r.customTypeLabel,
                satisfaction: r.satisfaction,
                memo: r.memo
            )
        }
        let encoder = JSONEncoder()
        encoder.outputFormatting = [.prettyPrinted, .sortedKeys]
        return try encoder.encode(dtos)
    }

    public static func fromJson(_ data: Data) -> [WorkoutRecord] {
        guard !data.isEmpty,
              let dtos = try? JSONDecoder().decode([DTO].self, from: data)
        else { return [] }
        return dtos.compactMap { dto in
            guard let date = DayDate.parse(dto.date),
                  let type = WorkoutType(rawValue: dto.type),
                  let intensity = Intensity(rawValue: dto.intensity)
            else { return nil }
            return WorkoutRecord(
                date: date,
                type: type,
                durationMin: dto.durationMin,
                intensity: intensity,
                id: dto.id,
                customTypeLabel: dto.customTypeLabel,
                satisfaction: dto.satisfaction,
                memo: dto.memo
            )
        }
    }
}
