// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "ZegoExpressEngine",
    platforms: [.iOS(.v11)],
    products: [
        .library(name: "ZegoExpressEngine", targets: ["ZegoExpressEngine"])
    ],
    targets: [
        .binaryTarget(
            name: "ZegoExpressEngine",
            url: "https://artifact-node.zego.cloud/generic/rtc/public/native/ZegoExpressVideo/ios/ZegoExpressVideo-ios-shared-objc.zip?version=3.25.0.50976",
            checksum: "41356359790ab44d80e442bf60c6c39c4d7e318fd208315c550f81be9ef380a8"
        )
    ]
)
