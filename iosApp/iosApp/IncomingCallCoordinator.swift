import AVFAudio
import CallKit
import UIKit

private extension Notification.Name {
    static let truelineZegoEnd = Notification.Name("trueline.listener.zego.end")
    static let truelineIncomingCallStart = Notification.Name("trueline.listener.incoming.start")
    static let truelineIncomingCallAccept = Notification.Name("trueline.listener.incoming.accept")
    static let truelineIncomingCallStop = Notification.Name("trueline.listener.incoming.stop")
    static let truelineIncomingCallAccepted = Notification.Name("trueline.listener.incoming.accepted")
    static let truelineIncomingCallDeclined = Notification.Name("trueline.listener.incoming.declined")
}

/// Presents incoming listener calls using the iOS system call UI. Zego still owns
/// the media room; CallKit owns the ringtone, lock-screen controls, and audio route.
final class IncomingCallCoordinator: NSObject, CXProviderDelegate {
    static let shared = IncomingCallCoordinator()

    private lazy var provider: CXProvider = {
        let configuration = CXProviderConfiguration(localizedName: "TrueLine")
        configuration.supportsVideo = false
        configuration.maximumCallsPerCallGroup = 1
        configuration.maximumCallGroups = 1
        configuration.includesCallsInRecents = false
        return CXProvider(configuration: configuration)
    }()
    private let callController = CXCallController()
    private var activeSessionID: String?
    private var activeCallUUID: UUID?
    private var isAccepted = false

    private override init() {
        super.init()
        provider.setDelegate(self, queue: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(showIncomingCall(_:)), name: .truelineIncomingCallStart, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(answerIncomingCall(_:)), name: .truelineIncomingCallAccept, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(endIncomingCall(_:)), name: .truelineIncomingCallStop, object: nil)
    }

    @objc private func showIncomingCall(_ notification: Notification) {
        guard
            let info = notification.userInfo,
            let sessionID = info["sessionId"] as? String,
            !sessionID.isEmpty
        else { return }
        presentIncomingCall(sessionID: sessionID, callerName: info["callerName"] as? String ?? "Customer")
    }

    func reportIncomingCall(sessionID: String, callerName: String) {
        DispatchQueue.main.async { [weak self] in
            self?.presentIncomingCall(sessionID: sessionID, callerName: callerName)
        }
    }

    private func presentIncomingCall(sessionID: String, callerName: String) {
        if activeSessionID == sessionID { return }

        if let existingUUID = activeCallUUID {
            provider.reportCall(with: existingUUID, endedAt: Date(), reason: .unanswered)
        }

        let uuid = UUID()
        activeSessionID = sessionID
        activeCallUUID = uuid
        isAccepted = false

        let update = CXCallUpdate()
        update.remoteHandle = CXHandle(type: .generic, value: callerName)
        update.localizedCallerName = callerName
        update.hasVideo = false
        update.supportsDTMF = false
        update.supportsHolding = false
        update.supportsGrouping = false
        update.supportsUngrouping = false

        provider.reportNewIncomingCall(with: uuid, update: update) { [weak self] error in
            guard let self else { return }
            if error != nil {
                self.resetActiveCall()
            }
        }
    }

    @objc private func answerIncomingCall(_ notification: Notification) {
        guard
            let requestedSessionID = notification.userInfo?["sessionId"] as? String,
            requestedSessionID == activeSessionID,
            let uuid = activeCallUUID
        else { return }

        // The system CallKit answer action already reached the shared flow.
        // A second programmatic transaction would be rejected by CallKit.
        if isAccepted { return }

        let action = CXAnswerCallAction(call: uuid)
        callController.request(CXTransaction(action: action)) { _ in }
    }

    @objc private func endIncomingCall(_ notification: Notification) {
        guard let sessionID = notification.userInfo?["sessionId"] as? String,
              sessionID == activeSessionID,
              let uuid = activeCallUUID
        else { return }
        provider.reportCall(with: uuid, endedAt: Date(), reason: isAccepted ? .remoteEnded : .unanswered)
        resetActiveCall()
    }

    func providerDidReset(_ provider: CXProvider) {
        let sessionID = activeSessionID
        let wasAccepted = isAccepted
        resetActiveCall()
        if wasAccepted {
            NotificationCenter.default.post(name: .truelineZegoEnd, object: nil)
        } else if sessionID != nil {
            NotificationCenter.default.post(name: .truelineIncomingCallDeclined, object: nil)
        }
    }

    func provider(_ provider: CXProvider, perform action: CXAnswerCallAction) {
        guard action.callUUID == activeCallUUID, activeSessionID != nil else {
            action.fail()
            return
        }
        isAccepted = true
        action.fulfill()
        NotificationCenter.default.post(name: .truelineIncomingCallAccepted, object: nil)
    }

    func provider(_ provider: CXProvider, perform action: CXEndCallAction) {
        guard action.callUUID == activeCallUUID else {
            action.fail()
            return
        }
        let wasAccepted = isAccepted
        resetActiveCall()
        action.fulfill()
        if wasAccepted {
            NotificationCenter.default.post(name: .truelineZegoEnd, object: nil)
        } else {
            NotificationCenter.default.post(name: .truelineIncomingCallDeclined, object: nil)
        }
    }

    func provider(_ provider: CXProvider, didActivate audioSession: AVAudioSession) {
        // Zego configures the active session when the listener joins the room.
    }

    func provider(_ provider: CXProvider, didDeactivate audioSession: AVAudioSession) {}

    private func resetActiveCall() {
        activeSessionID = nil
        activeCallUUID = nil
        isAccepted = false
    }
}
