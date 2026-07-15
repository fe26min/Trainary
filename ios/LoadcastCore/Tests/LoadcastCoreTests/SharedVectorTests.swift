import Foundation
import XCTest
@testable import LoadcastCore

/// spec/test-vectors.json 공유 벡터 검증.
/// Android(core)의 SharedVectorTest와 반드시 같은 파일·같은 기대값을 사용한다.
final class SharedVectorTests: XCTestCase {

    // MARK: - 벡터 파일 로드 (#filePath 기준 저장소 루트의 spec/test-vectors.json)

    private struct Vectors: Decodable {
        let loadCases: [LoadCase]
        let acwrCases: [AcwrCase]
        let weeklySummaryCases: [WeeklySummaryCase]
        let fatigueCases: [FatigueCase]
        let recommendationCases: [RecommendationCase]
    }

    private struct RecordJSON: Decodable {
        let date: String
        let type: String
        let durationMin: Int
        let intensity: String

        var record: WorkoutRecord {
            WorkoutRecord(
                date: DayDate.parse(date)!,
                type: WorkoutType(rawValue: type)!,
                durationMin: durationMin,
                intensity: Intensity(rawValue: intensity)!
            )
        }
    }

    private struct LoadCase: Decodable {
        let name: String
        let durationMin: Int
        let intensity: String
        let expectedLoad: Int
    }

    private struct AcwrCase: Decodable {
        let name: String
        let acuteLoad: Int
        let chronicLoad: Int
        let expectedAcwr: Double?
        let expectedStatus: String
    }

    private struct WeeklySummaryCase: Decodable {
        struct Expected: Decodable {
            let totalMinutes: Int
            let totalLoad: Int
            let highCount: Int
            let restDays: Int
        }
        let name: String
        let today: String
        let records: [RecordJSON]
        let expected: Expected
    }

    private struct FatigueCase: Decodable {
        let name: String
        let today: String
        let records: [RecordJSON]
        let expected: [String: String]
    }

    private struct RecommendationCase: Decodable {
        struct Expected: Decodable {
            let kind: String
            let recordedCount: Int?
            let requiredCount: Int?
            let state: String?
            let estimate: Bool?
            let reasonCodes: [String]?
            let acwr: Double?
            let loadStatus: String?
        }
        let name: String
        let today: String
        let firstUseDate: String
        let records: [RecordJSON]
        let expected: Expected
    }

    private static let vectors: Vectors = {
        // .../ios/LoadcastCore/Tests/LoadcastCoreTests/SharedVectorTests.swift → 저장소 루트
        let repoRoot = URL(fileURLWithPath: #filePath)
            .deletingLastPathComponent() // LoadcastCoreTests
            .deletingLastPathComponent() // Tests
            .deletingLastPathComponent() // LoadcastCore
            .deletingLastPathComponent() // ios
        let url = repoRoot.appendingPathComponent("spec/test-vectors.json")
        let data = try! Data(contentsOf: url)
        return try! JSONDecoder().decode(Vectors.self, from: data)
    }()

    // MARK: - 케이스

    func testLoadCases() {
        for c in Self.vectors.loadCases {
            let actual = LoadCalculator.sessionLoad(
                durationMin: c.durationMin,
                intensity: Intensity(rawValue: c.intensity)!
            )
            XCTAssertEqual(actual, c.expectedLoad, c.name)
        }
    }

    func testAcwrCases() {
        for c in Self.vectors.acwrCases {
            let acwr = Acwr.ratio(acuteLoad: c.acuteLoad, chronicLoad: c.chronicLoad)
            if let expected = c.expectedAcwr {
                XCTAssertNotNil(acwr, c.name)
                XCTAssertEqual(acwr!, expected, accuracy: 1e-12, c.name)
            } else {
                XCTAssertNil(acwr, c.name)
            }
            XCTAssertEqual(Acwr.status(acwr).rawValue, c.expectedStatus, c.name)
        }
    }

    func testWeeklySummaryCases() {
        for c in Self.vectors.weeklySummaryCases {
            let summary = WeeklyAnalyzer.summarize(
                records: c.records.map(\.record),
                today: DayDate.parse(c.today)!
            )
            XCTAssertEqual(summary.totalMinutes, c.expected.totalMinutes, c.name)
            XCTAssertEqual(summary.totalLoad, c.expected.totalLoad, c.name)
            XCTAssertEqual(summary.highCount, c.expected.highCount, c.name)
            XCTAssertEqual(summary.restDays, c.expected.restDays, c.name)
        }
    }

    func testFatigueCases() {
        for c in Self.vectors.fatigueCases {
            let actual = BodyFatigue.estimate(
                records: c.records.map(\.record),
                today: DayDate.parse(c.today)!
            )
            for (group, level) in c.expected {
                XCTAssertEqual(
                    actual[BodyGroup(rawValue: group)!]?.rawValue,
                    level,
                    "\(c.name) / \(group)"
                )
            }
        }
    }

    func testRecommendationCases() {
        for c in Self.vectors.recommendationCases {
            let result = Recommender.recommend(
                records: c.records.map(\.record),
                today: DayDate.parse(c.today)!,
                firstUseDate: DayDate.parse(c.firstUseDate)!
            )
            switch (c.expected.kind, result) {
            case ("INSUFFICIENT_DATA", .insufficientData(let recordedCount, let requiredCount)):
                XCTAssertEqual(recordedCount, c.expected.recordedCount, c.name)
                XCTAssertEqual(requiredCount, c.expected.requiredCount, c.name)
            case ("RECOMMENDATION", .recommendation(let rec)):
                XCTAssertEqual(rec.state.rawValue, c.expected.state, c.name)
                XCTAssertEqual(rec.estimate, c.expected.estimate, c.name)
                XCTAssertEqual(rec.reasons.map(\.rawValue), c.expected.reasonCodes, c.name)
                if let expectedAcwr = c.expected.acwr {
                    XCTAssertNotNil(rec.acwr, c.name)
                    XCTAssertEqual(rec.acwr!, expectedAcwr, accuracy: 1e-12, c.name)
                } else {
                    XCTAssertNil(rec.acwr, c.name)
                }
                XCTAssertEqual(rec.loadStatus.rawValue, c.expected.loadStatus, c.name)
            default:
                XCTFail("\(c.name): expected \(c.expected.kind), got \(result)")
            }
        }
    }

    func testDayDateParsingAndArithmetic() {
        let d = DayDate.parse("2026-07-15")!
        XCTAssertEqual(d.adding(days: -6), DayDate.parse("2026-07-09")!)
        XCTAssertEqual(d.adding(days: -27), DayDate.parse("2026-06-18")!)
        XCTAssertEqual(DayDate.parse("2026-03-01")!.adding(days: -1), DayDate.parse("2026-02-28")!)
        XCTAssertEqual(DayDate.parse("2024-03-01")!.adding(days: -1), DayDate.parse("2024-02-29")!) // 윤년
        XCTAssertEqual(DayDate(year: 1970, month: 1, day: 1).daysSinceEpoch, 0)
    }
}
