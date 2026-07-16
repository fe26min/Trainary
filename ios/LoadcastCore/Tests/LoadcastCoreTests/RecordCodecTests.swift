import Foundation
import XCTest
@testable import LoadcastCore

final class RecordCodecTests: XCTestCase {

    private let sample = [
        WorkoutRecord(
            date: DayDate.parse("2026-07-14")!,
            type: .crossfit,
            durationMin: 60,
            intensity: .high,
            id: "id-1",
            satisfaction: 4,
            memo: "오늘 WOD 정말 힘들었다."
        ),
        WorkoutRecord(
            date: DayDate.parse("2026-07-12")!,
            type: .other,
            durationMin: 35,
            intensity: .low,
            id: "id-2",
            customTypeLabel: "클라이밍"
        ),
    ]

    func testRoundTripPreservesAllFields() throws {
        let decoded = RecordCodec.fromJson(try RecordCodec.toJson(sample))
        XCTAssertEqual(decoded, sample)
    }

    func testEmptyInputDecodesToEmptyList() {
        XCTAssertTrue(RecordCodec.fromJson(Data()).isEmpty)
        XCTAssertTrue(RecordCodec.fromJson(Data("[]".utf8)).isEmpty)
    }

    func testDayDateIsoStringRoundTrip() {
        for iso in ["2026-07-15", "2024-02-29", "1970-01-01", "1999-12-31"] {
            XCTAssertEqual(DayDate.parse(iso)?.isoString, iso)
        }
    }

    /// Android RecordCodec(Gson) 출력과의 포맷 호환 확인.
    func testDecodesAndroidGsonFormat() {
        let json = """
        [{"date":"2026-07-14","type":"CROSSFIT","durationMin":60,"intensity":"HIGH","id":"id-1","satisfaction":4,"memo":"m"}]
        """
        let decoded = RecordCodec.fromJson(Data(json.utf8))
        XCTAssertEqual(decoded.count, 1)
        XCTAssertEqual(decoded.first?.id, "id-1")
        XCTAssertEqual(decoded.first?.load, 180)
    }
}
