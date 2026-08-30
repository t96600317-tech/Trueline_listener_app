import SwiftUI

@main
struct iOSApp: App {
    init() {
        _ = ZegoAudioCallCoordinator.shared
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
