// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "LoadcastCore",
    platforms: [
        .iOS(.v16),
        .macOS(.v13), // macOS에서 `swift test` 실행용
    ],
    products: [
        .library(name: "LoadcastCore", targets: ["LoadcastCore"]),
    ],
    targets: [
        .target(name: "LoadcastCore"),
        .testTarget(name: "LoadcastCoreTests", dependencies: ["LoadcastCore"]),
    ]
)
