# Privacy Policy for Lockeyy

**Last updated: October 2026**

Welcome to **Lockeyy** ("we", "our", or "the app"). We respect your privacy and are committed to protecting it. This Privacy Policy explains our practices regarding your data when you use the Lockeyy application.

---

### 1. Zero Data Collection Guarantee
Lockeyy operates **entirely offline** on your device:
- **No Personal Data Collected**: We do not collect, store, or transmit your personal information, name, email, phone number, contacts, or location.
- **No Network Transmission**: Lockeyy does not upload your PIN, pattern, biometric data, or list of installed apps to any external server, third-party analytics, or cloud service.
- **No Third-Party Trackers**: We do not include any third-party advertising SDKs, tracking pixels, or data brokerage tools.

---

### 2. Device Permissions and How They Are Used
To provide app locking and security features, Lockeyy requires specific Android permissions. Each permission is used solely for on-device functionality:

#### A. Accessibility Service (`android.permission.BIND_ACCESSIBILITY_SERVICE`)
- **Purpose**: Lockeyy uses the Android Accessibility Service API exclusively to detect when a user attempts to launch a protected (locked) application.
- **Data Handling**: The Accessibility Service only monitors foreground window changes (`TYPE_WINDOW_STATE_CHANGED`) to verify if the foreground application package matches your locked apps list. No text input, keystrokes, passwords, or on-screen content are read, recorded, or saved.

#### B. Appear on Top / Overlay (`android.permission.SYSTEM_ALERT_WINDOW`)
- **Purpose**: To immediately draw the secure lock screen overlay over protected applications before their contents become visible.
- **Data Handling**: Used purely for displaying the local lock interface.

#### C. Query All Packages (`android.permission.QUERY_ALL_PACKAGES`)
- **Purpose**: To display the list of installed applications on your device within Lockeyy, allowing you to select which apps you wish to protect.
- **Data Handling**: The list of installed applications is processed only in local memory on your device. It is never transmitted off your device.

#### D. Usage Stats (`android.permission.PACKAGE_USAGE_STATS`)
- **Purpose**: As an optional fallback mechanism on devices where Accessibility services may be suspended by aggressive OEM battery managers, Usage Stats is used to identify the currently active foreground app package name.

#### E. Biometric Authentication (`android.permission.USE_BIOMETRIC`)
- **Purpose**: Enables you to unlock protected applications using your device's fingerprint or face sensor.
- **Data Handling**: Biometric authentication is handled entirely by the Android operating system's secure hardware enclave (TEE/Keystore). Lockeyy never has access to your raw biometric data.

#### F. Post Notifications (`android.permission.POST_NOTIFICATIONS`)
- **Purpose**: Required on Android 13+ to show the persistent foreground service notification, ensuring the app lock service remains actively running in the background.

---

### 3. Data Security & Storage
- All user preferences, including the list of locked apps and hashed security credentials, are stored locally on your device within private, sandboxed app storage.
- Passcodes and patterns are hashed using cryptographic one-way hashing before storage.
- Application backup (`allowBackup`) is explicitly disabled to prevent extraction of lock credentials via USB debugging or external backup utilities.

---

### 4. Children’s Privacy
Lockeyy does not address anyone under the age of 13 or knowingly collect any information from children.

---

### 5. Changes to This Privacy Policy
We may update our Privacy Policy from time to time. Any changes will be published in this document with an updated revision date.

---

### 6. Contact Us
If you have any questions or suggestions regarding this Privacy Policy, please contact the developer via the official app store support link or repository issues.
