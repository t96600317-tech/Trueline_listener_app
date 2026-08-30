import AVFAudio
import UIKit
import ZegoExpressEngine

private extension Notification.Name {
    static let truelineZegoStart = Notification.Name("trueline.listener.zego.start")
    static let truelineZegoEnd = Notification.Name("trueline.listener.zego.end")
    static let truelineZegoEnded = Notification.Name("trueline.listener.zego.ended")
    static let truelineZegoFailed = Notification.Name("trueline.listener.zego.failed")
}

final class ZegoAudioCallCoordinator: NSObject, ZegoEventHandler {
    static let shared = ZegoAudioCallCoordinator()

    private var engine: ZegoExpressEngine?
    private var roomID = ""
    private var streamID = ""
    private weak var callViewController: ZegoAudioCallViewController?

    private override init() {
        super.init()
        NotificationCenter.default.addObserver(self, selector: #selector(startCall(_:)), name: .truelineZegoStart, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(endCallFromNotification), name: .truelineZegoEnd, object: nil)
    }

    @objc private func startCall(_ notification: Notification) {
        guard
            let info = notification.userInfo,
            let appID = (info["appId"] as? NSNumber)?.uint32Value,
            let token = info["token"] as? String,
            let roomID = info["roomId"] as? String,
            let userID = info["userId"] as? String,
            let userName = info["userName"] as? String,
            !token.isEmpty,
            !roomID.isEmpty,
            !userID.isEmpty
        else {
            reportFailure("Voice connection failed: invalid server call credentials")
            return
        }

        AVAudioSession.sharedInstance().requestRecordPermission { [weak self] granted in
            DispatchQueue.main.async {
                guard granted else {
                    self?.reportFailure("Microphone permission was denied")
                    return
                }
                self?.joinRoom(appID: appID, token: token, roomID: roomID, userID: userID, userName: userName, targetName: info["targetUserName"] as? String ?? "Customer")
            }
        }
    }

    private func joinRoom(appID: UInt32, token: String, roomID: String, userID: String, userName: String, targetName: String) {
        tearDownCall(reportEnd: false)
        self.roomID = roomID
        self.streamID = "trueline_\(userID)_\(roomID)"

        do {
            let session = AVAudioSession.sharedInstance()
            try session.setCategory(.playAndRecord, mode: .voiceChat, options: [.defaultToSpeaker, .allowBluetooth])
            try session.setActive(true)
        } catch {
            reportFailure("Unable to configure audio: \(error.localizedDescription)")
            return
        }

        let screen = ZegoAudioCallViewController(targetName: targetName) { [weak self] in
            self?.endCallFromNotification()
        }
        callViewController = screen
        presentingViewController()?.present(screen, animated: true)

        let profile = ZegoEngineProfile()
        profile.appID = appID
        profile.appSign = ""
        profile.scenario = .default
        engine = ZegoExpressEngine.createEngine(with: profile, eventHandler: self)
        let config = ZegoRoomConfig()
        config.token = token
        config.isUserStatusNotify = true
        engine?.loginRoom(roomID, user: ZegoUser(userID: userID, userName: userName), config: config)
        screen.setStatus("Connecting…")
    }

    func onRoomStateUpdate(_ state: ZegoRoomState, errorCode: Int32, extendedData: [AnyHashable: Any]?, roomID: String) {
        guard roomID == self.roomID else { return }
        switch state {
        case .connected:
            engine?.muteMicrophone(false)
            engine?.startPublishingStream(streamID)
            callViewController?.setStatus("Connected")
        case .disconnected where errorCode != 0:
            reportFailure("Zego room login failed (code \(errorCode))")
        default:
            break
        }
    }

    func onRoomStreamUpdate(_ updateType: ZegoUpdateType, streamList: [ZegoStream], extendedData: [AnyHashable: Any]?, roomID: String) {
        guard roomID == self.roomID else { return }
        for stream in streamList {
            // Do not play the listener's own published microphone stream; it
            // causes audible echo in a two-person audio room.
            guard stream.streamID != self.streamID else { continue }
            if updateType == .add {
                engine?.startPlayingStream(stream.streamID, canvas: nil)
            } else {
                engine?.stopPlayingStream(stream.streamID)
                // End the native call screen when the customer leaves the
                // one-to-one Zego room instead of leaving a stale call open.
                DispatchQueue.main.async { [weak self] in
                    self?.endCallFromNotification()
                }
                return
            }
        }
    }

    @objc private func endCallFromNotification() {
        tearDownCall(reportEnd: true)
    }

    private func tearDownCall(reportEnd: Bool) {
        guard !roomID.isEmpty || engine != nil else { return }
        engine?.stopPublishingStream()
        if !roomID.isEmpty {
            engine?.logoutRoom(roomID)
        }
        ZegoExpressEngine.destroy(nil)
        engine = nil
        roomID = ""
        streamID = ""
        callViewController?.dismiss(animated: true)
        callViewController = nil
        if reportEnd {
            NotificationCenter.default.post(name: .truelineZegoEnded, object: nil)
        }
    }

    private func reportFailure(_ message: String) {
        tearDownCall(reportEnd: false)
        NotificationCenter.default.post(name: .truelineZegoFailed, object: nil, userInfo: ["message": message])
    }

    private func presentingViewController() -> UIViewController? {
        let root = UIApplication.shared.connectedScenes
            .compactMap { $0 as? UIWindowScene }
            .flatMap(\.windows)
            .first(where: \.isKeyWindow)?
            .rootViewController
        return root?.topMostViewController()
    }
}

private extension UIViewController {
    func topMostViewController() -> UIViewController {
        if let presentedViewController { return presentedViewController.topMostViewController() }
        if let navigationController = self as? UINavigationController { return navigationController.visibleViewController?.topMostViewController() ?? navigationController }
        if let tabBarController = self as? UITabBarController { return tabBarController.selectedViewController?.topMostViewController() ?? tabBarController }
        return self
    }
}

private final class ZegoAudioCallViewController: UIViewController {
    private let targetName: String
    private let onEnd: () -> Void
    private let statusLabel = UILabel()

    init(targetName: String, onEnd: @escaping () -> Void) {
        self.targetName = targetName
        self.onEnd = onEnd
        super.init(nibName: nil, bundle: nil)
    }

    required init?(coder: NSCoder) { nil }

    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = .systemBackground
        let title = UILabel()
        title.text = targetName
        title.font = .preferredFont(forTextStyle: .title2)
        title.textAlignment = .center
        statusLabel.text = "Connecting…"
        statusLabel.textAlignment = .center
        let endButton = UIButton(type: .system)
        endButton.setTitle("End call", for: .normal)
        endButton.tintColor = .systemRed
        endButton.addAction(UIAction { [weak self] _ in self?.onEnd() }, for: .touchUpInside)
        let stack = UIStackView(arrangedSubviews: [title, statusLabel, endButton])
        stack.axis = .vertical
        stack.spacing = 20
        stack.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(stack)
        NSLayoutConstraint.activate([
            stack.centerXAnchor.constraint(equalTo: view.centerXAnchor),
            stack.centerYAnchor.constraint(equalTo: view.centerYAnchor),
            stack.leadingAnchor.constraint(greaterThanOrEqualTo: view.leadingAnchor, constant: 24),
            stack.trailingAnchor.constraint(lessThanOrEqualTo: view.trailingAnchor, constant: -24)
        ])
    }

    func setStatus(_ status: String) { statusLabel.text = status }
}
