import SwiftUI

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) private var appDelegate

    init() {
        _ = ZegoAudioCallCoordinator.shared
        _ = IncomingCallCoordinator.shared
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
