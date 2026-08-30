# Testing and emulator delivery

The GitHub Actions **Mobile CI** workflow runs on pull requests and `main` pushes.

- The required Android job runs `:shared:testAndroidHostTest`, builds the debug APK, and retains the test reports and APK as workflow artifacts.
- The macOS iOS job runs `:shared:iosSimulatorArm64Test` and an unsigned simulator build. It is advisory while native iOS bridges are still being implemented, so an iOS failure does not block Android delivery.
- A manually dispatched run also uploads the exact Android debug APK to the configured public Appetize application.

## Run locally

```bash
./gradlew :shared:testAndroidHostTest :androidApp:assembleDebug
```

On a Mac with Xcode installed, also run:

```bash
./gradlew :shared:iosSimulatorArm64Test
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -configuration Debug -sdk iphonesimulator CODE_SIGNING_ALLOWED=NO build
```

## Appetize setup

Create a public Android app in Appetize, then add these repository-level GitHub Actions secrets:

| Secret | Purpose |
| --- | --- |
| `APPETIZE_API_TOKEN` | Appetize API token used only by the upload job. |
| `APPETIZE_LISTENER_PUBLIC_KEY` | Public key of the pre-created listener Appetize app. |

Open **Actions → Mobile CI → Run workflow** to upload a build. The workflow summary contains the public Appetize link after a successful upload. Keep the Appetize public key shareable; never put the API token in source code or an artifact.

Appetize is an emulator smoke-test environment. It is suitable for navigation and mocked/backend-driven flows, but it does not replace physical-device testing for inbound Zego calls, push notifications, audio routing, or telephony behavior.
