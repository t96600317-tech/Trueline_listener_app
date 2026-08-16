# Implementation Plan - TrueLine Listener Base Design & Onboarding Step 1

This plan covers the initial setup of the TrueLine Listener design system and the implementation of the first step of the onboarding flow: Phone Number Input and OTP Verification.

## User Review Required

> [!IMPORTANT]
> The project is a Kotlin Multiplatform (KMP) project using Compose Multiplatform. I will implement the UI and ViewModel in the `shared` module to maximize code sharing between Android and iOS.

## Proposed Changes

### 🎨 Design System (Theme & Color Tokens)

I will establish the theme based on the provided brand color palette.

#### [NEW] [Color.kt](file:///D:/flutter_project/Trueline_listener/shared/src/commonMain/kotlin/com/example/trueline_listener/ui/theme/Color.kt)
- Define color constants: `Primary`, `Secondary`, `Accent`, `Dark`, `Light`, `OnlineSuccess`.

#### [NEW] [Theme.kt](file:///D:/flutter_project/Trueline_listener/shared/src/commonMain/kotlin/com/example/trueline_listener/ui/theme/Theme.kt)
- Configure `MaterialTheme` with custom `ColorScheme` (Light/Dark).
- Use `Light` (#F4F8F9) as background and `Primary` (#2D6A68) for headers/buttons.

#### [NEW] [Type.kt](file:///D:/flutter_project/Trueline_listener/shared/src/commonMain/kotlin/com/example/trueline_listener/ui/theme/Type.kt)
- Define standard typography using the brand's preferred styles (clean, trustworthy).

---

### 📱 Onboarding Flow - Step 1

#### [NEW] [OnboardingViewModel.kt](file:///D:/flutter_project/Trueline_listener/shared/src/commonMain/kotlin/com/example/trueline_listener/onboarding/OnboardingViewModel.kt)
- Manage state for:
    - Phone number (10-digit validation).
    - OTP (6 digits).
    - Resend timer (30 seconds).
    - Current onboarding step (Phone Input vs. OTP).
- Logic for "Send OTP" and "Verify & Continue".

#### [NEW] [PhoneInputScreen.kt](file:///D:/flutter_project/Trueline_listener/shared/src/commonMain/kotlin/com/example/trueline_listener/onboarding/PhoneInputScreen.kt)
- UI for Screen 1A.
- Fixed +91 prefix.
- Accent Amber button enabled only on valid input.
- Trust note and 18+ requirement.

#### [NEW] [OtpVerificationScreen.kt](file:///D:/flutter_project/Trueline_listener/shared/src/commonMain/kotlin/com/example/trueline_listener/onboarding/OtpVerificationScreen.kt)
- UI for Screen 1B.
- 6 individual digit boxes with auto-focus logic.
- Countdown timer for "Resend OTP".

#### [MODIFY] [App.kt](file:///D:/flutter_project/Trueline_listener/shared/src/commonMain/kotlin/com/example/trueline_listener/App.kt)
- Wrap the app in `TrueLineTheme`.
- Implement navigation logic between `PhoneInputScreen` and `OtpVerificationScreen` based on `OnboardingViewModel` state.

## Verification Plan

### Automated Tests
- Unit tests for `OnboardingViewModel` (validation logic, timer state).

### Manual Verification
- Deploy to Android emulator.
- Verify colors and spacing match the PRD.
- Enter 10-digit number -> Check "Send OTP" button state.
- Tap "Send OTP" -> Verify navigation to OTP screen.
- Verify individual OTP boxes auto-focus behavior.
- Verify 30s countdown timer resets and enables "Resend OTP".
