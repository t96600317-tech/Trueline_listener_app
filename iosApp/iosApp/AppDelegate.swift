import PushKit
import UIKit
import UserNotifications

final class AppDelegate: NSObject, UIApplicationDelegate, PKPushRegistryDelegate {
    private let voipPushTokenKey = "trueline.listener.voip-token"
    private var pushRegistry: PKPushRegistry?

    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        let registry = PKPushRegistry(queue: .main)
        registry.delegate = self
        registry.desiredPushTypes = [.voIP]
        pushRegistry = registry

        UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .badge, .sound]) { _, _ in }
        return true
    }

    func pushRegistry(_ registry: PKPushRegistry, didUpdate pushCredentials: PKPushCredentials, for type: PKPushType) {
        guard type == .voIP else { return }
        let token = pushCredentials.token.map { String(format: "%02x", $0) }.joined()
        UserDefaults.standard.set(token, forKey: voipPushTokenKey)
        NotificationCenter.default.post(name: Notification.Name("trueline.listener.voip-token.updated"), object: nil)
    }

    func pushRegistry(_ registry: PKPushRegistry, didInvalidatePushTokenFor type: PKPushType) {
        guard type == .voIP else { return }
        UserDefaults.standard.removeObject(forKey: voipPushTokenKey)
    }

    func pushRegistry(
        _ registry: PKPushRegistry,
        didReceiveIncomingPushWith payload: PKPushPayload,
        for type: PKPushType,
        completion: @escaping () -> Void
    ) {
        defer { completion() }
        guard type == .voIP else { return }
        let data = payload.dictionaryPayload
        guard let sessionID = data["session_id"] as? String, !sessionID.isEmpty else { return }
        let callerName = (data["caller_name"] as? String)?.trimmingCharacters(in: .whitespacesAndNewlines)
        IncomingCallCoordinator.shared.reportIncomingCall(
            sessionID: sessionID,
            callerName: callerName?.isEmpty == false ? callerName! : "Customer"
        )
    }
}
