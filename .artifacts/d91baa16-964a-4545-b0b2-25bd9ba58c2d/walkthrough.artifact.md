# Walkthrough - TrueLine Listener Design System & Onboarding Step 1

I have configured the base design system and implemented the first step of the onboarding flow (Phone + OTP Verification) as per the PRD.

## Changes Made

### 🎨 Design System
- **[Color.kt](file:///D:/flutter_project/Trueline_listener/shared/src/commonMain/kotlin/com/example/trueline_listener/ui/theme/Color.kt)**: Defined the brand color palette (#2D6A68, #5FA8D3, #F2A65A, etc.).
- **[Type.kt](file:///D:/flutter_project/Trueline_listener/shared/src/commonMain/kotlin/com/example/trueline_listener/ui/theme/Type.kt)**: Set up standard Material 3 typography.
- **[Theme.kt](file:///D:/flutter_project/Trueline_listener/shared/src/commonMain/kotlin/com/example/trueline_listener/ui/theme/Theme.kt)**: Created `TrueLineTheme` to apply these colors and typography across the app.

### 📱 Onboarding Step 1
- **[OnboardingViewModel.kt](file:///D:/flutter_project/Trueline_listener/shared/src/commonMain/kotlin/com/example/trueline_listener/onboarding/OnboardingViewModel.kt)**:
    - Manages phone and OTP state.
    - Implements 10-digit phone validation and 6-digit OTP validation.
    - Handles a 30-second "Resend OTP" countdown timer.
    - Manages transitions between `PHONE_INPUT` and `OTP_VERIFICATION` steps.
- **[PhoneInputScreen.kt](file:///D:/flutter_project/Trueline_listener/shared/src/commonMain/kotlin/com/example/trueline_listener/onboarding/PhoneInputScreen.kt)**:
    - Screen 1A with fixed +91 prefix and Indian number validation.
    - Amber "Send OTP" button enabled only for valid input.
- **[OtpVerificationScreen.kt](file:///D:/flutter_project/Trueline_listener/shared/src/commonMain/kotlin/com/example/trueline_listener/onboarding/OtpVerificationScreen.kt)**:
    - Screen 1B with 6 individual digit input boxes.
    - Auto-focus logic to the next box on input and backspace handling.
    - Integrated countdown timer for resending OTP.

### 🛠️ App Integration & System UI
- **[MainActivity.kt](file:///D:/flutter_project/Trueline_listener/androidApp/src/main/kotlin/com/example/trueline_listener/MainActivity.kt)**:
    - Enabled `isAppearanceLightStatusBars = true` to ensure system icons (battery, clock, etc.) are dark and visible against the light theme background.
- **[App.kt](file:///D:/flutter_project/Trueline_listener/shared/src/commonMain/kotlin/com/example/trueline_listener/App.kt)**:
    - Wrapped the application in `TrueLineTheme`.
    - Switched to `statusBarsPadding()` and `navigationBarsPadding()` on the root container for precise system inset handling.
    - Integrated `OnboardingViewModel` and set up conditional navigation.

### 📱 Onboarding Step 2: Basic Profile
- **[BasicProfileScreen.kt](file:///D:/flutter_project/Trueline_listener/shared/src/commonMain/kotlin/com/example/trueline_listener/onboarding/BasicProfileScreen.kt)**:
    - Implemented a clean, scrollable form for profile data.
    - Fields: Full Name, Age (with 18+ validation), Multi-select Languages, City/State, and Bio.
    - Uses `FlowRow` for language chips and `FilterChip` for an intuitive selection experience.
    - Bio includes a real-time character counter (max 200).
- **[OnboardingViewModel.kt](file:///D:/flutter_project/Trueline_listener/shared/src/commonMain/kotlin/com/example/trueline_listener/onboarding/OnboardingViewModel.kt)**:
    - Added state management for all Step 2 fields.
    - Implemented validation logic (`isProfileValid`) that enables the "Next" button only when all mandatory fields (Name, Age 18+, Languages, City/State) are filled.

### 📱 Onboarding Step 4: Face Verification
- **[FaceVerificationScreen.kt](file:///D:/flutter_project/Trueline_listener/shared/src/commonMain/kotlin/com/example/trueline_listener/onboarding/FaceVerificationScreen.kt)**:
    - Implemented a camera placeholder UI with an oval face guide.
    - Dynamic instruction display for liveness prompts (e.g., "Blink", "Turn Head").
    - Handles capture, verification, success, and failure states.
- **[OnboardingViewModel.kt](file:///D:/flutter_project/Trueline_listener/shared/src/commonMain/kotlin/com/example/trueline_listener/onboarding/OnboardingViewModel.kt)**:
    - Added liveness simulation logic using coroutines.
    - Implemented retry logic (up to 3 attempts) and routing to manual review as per PRD requirements.

## Verification Results

### Manual Verification (Expected Behavior)
1. **Liveness Flow**: Start the check and observe prompts to "Blink" and "Turn Head" at 2-second intervals.
2. **Verification State**: A 3-second "Verifying..." loading state appears after capture.
3. **Success**: Navigates to the "Step 5: KYC Document" placeholder.
4. **Retry Handling**: If verification fails, a "Retry" button appears with the remaining attempt count. After 3 failures, the system automatically routes the user to a manual review path.
