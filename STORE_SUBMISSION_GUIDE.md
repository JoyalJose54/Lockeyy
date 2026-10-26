# 🚀 Lockeyy - App Store Submission Guide

This guide contains everything you need to successfully upload and publish **Lockeyy** on **Indus Appstore (PhonePe)**, **APKPure**, and **Uptodown**.

---

## 📦 1. Your Release APK & Keystore Details

| Item | Location / Value |
| :--- | :--- |
| **Release APK** | `app/build/outputs/apk/release/app-release.apk` |
| **Package Name (Application ID)** | `com.lockeyy.applock` |
| **Version Code / Name** | `1` / `1.0.0` |
| **Target SDK / Min SDK** | `35` (Android 15) / `26` (Android 8.0) |
| **Keystore File** | `app/release.keystore` |
| **Key Alias** | `lockeyy` |
| **Keystore / Key Password** | `lockeyyrelease` |
| **Validity** | Until Year 2054 (27 Years) |

> [!IMPORTANT]
> **CRITICAL: BACK UP YOUR KEYSTORE!**
> Keep a safe backup copy of `app/release.keystore` and its password. When you release version 1.0.1 or 2.0 in the future, app stores will **REJECT** the update if it is not signed with this exact keystore!

---

## 📝 2. Store Listing Content (Copy & Paste)

### App Title
`Lockeyy - App Lock & Privacy Shield`

### Short Description (Max 80 chars)
`Fast, sleek app locker with biometric, PIN & pattern security.`

### Full Description
```
Keep your personal applications, sensitive chats, photos, and private data completely safe with Lockeyy.

Lockeyy is a lightweight, AMOLED-optimized app locker designed for modern Android devices. Built with zero-latency protection, it instantly shields locked apps before anyone can sneak a peek.

✨ KEY FEATURES:
• Multi-Factor Unlock: Unlock instantly using Fingerprint, Face Biometrics, 4-digit PIN, or 3x3 Pattern.
• Instant Shield Protection: Prevents the 300ms preview flash common in other lockers.
• AMOLED Dark Design: Beautiful, battery-friendly OneUI-inspired dark theme.
• 100% Private & Offline: Lockeyy does not collect, store, or transmit your personal data. No internet connection required.
• Battery Efficient: Event-driven architecture with zero background battery drain.
• Samsung OEM Sync: Automatically integrates with OneUI security locks on supported Samsung Galaxy devices.

🔒 PERMISSIONS DISCLOSURE:
• Accessibility Service: Used strictly to detect when a locked app is opened in real-time so Lockeyy can display the lock screen. No on-screen content or keystrokes are read or recorded.
• Appear on Top (Overlay): Displays the security unlock screen over protected apps.
• Query All Packages: Allows you to view and select which installed apps you want to lock.
```

---

## 📋 3. Store Permission Justification (For Indus Appstore Review)

When submitting on **Indus Appstore Developer Console**, the reviewer may prompt you to explain why your app uses sensitive permissions. Use these pre-approved justification texts:

#### Q: Why does your app need `BIND_ACCESSIBILITY_SERVICE`?
> *"Lockeyy is an app locker application. The Accessibility Service is used solely to detect window state changes (when a user opens an application) in order to immediately display the security lock screen over user-selected protected apps. Lockeyy does not read, record, or transmit any user text, keystrokes, or on-screen content."*

#### Q: Why does your app need `QUERY_ALL_PACKAGES`?
> *"Lockeyy requires QUERY_ALL_PACKAGES so that users can view their list of installed applications inside the app and choose which applications they wish to lock and protect. The app package list is processed strictly on-device in local memory and is never transmitted off the device."*

#### Q: Why does your app need `SYSTEM_ALERT_WINDOW` (Appear on Top)?
> *"Used to draw the secure authentication PIN/pattern/biometric unlock dialog over foreground applications when a protected app is accessed."*

---

## 🌐 4. Privacy Policy URL
Both Indus Appstore and Uptodown require a live Privacy Policy URL.
We have generated [`PRIVACY_POLICY.md`](file:///d:/RSET/PROJECT/S%20Secure/PRIVACY_POLICY.md) in your project root.

**How to get a free live URL in 2 minutes:**
1. Open [gist.github.com](https://gist.github.com).
2. Paste the contents of `PRIVACY_POLICY.md`.
3. Name it `privacy-policy.md` and click **Create public gist**.
4. Copy the URL and paste it into the **Privacy Policy URL** field on Indus Appstore / APKPure / Uptodown.

---

## 🛠️ 5. Step-by-Step Upload Instructions

### A. Indus Appstore (developer.indusappstore.com)
1. Log in to your **Indus Appstore Developer Account**.
2. Click **Create New App** &rarr; Select **Android App**.
3. Upload `app/build/outputs/apk/release/app-release.apk`.
4. Fill in the App Title, Short Description, Full Description, and Privacy Policy URL from above.
5. Provide the Accessibility & Package justifications provided above.
6. Upload your app icon (`Gemini_Generated_Image_wvv37uwvv37uwvv3.png`) and 2-3 screenshots.
7. Click **Submit for Review**.

### B. APKPure (developer.apkpure.com)
1. Log in to **APKPure Developer Console**.
2. Click **Add App** &rarr; Upload `app-release.apk`.
3. Add the title and descriptions.
4. Submit for scanning and publication.

### C. Uptodown (developers.uptodown.com)

Uptodown is 100% free with no registration fee and accepts standard signed `.apk` files directly.

1. **Sign Up / Log In**:
   - Go to [developers.uptodown.com](https://developers.uptodown.com).
   - Sign up with your Google account or email.
   - Enter your developer / organization name (e.g. `Joyal Jose`).

2. **Add Your Application**:
   - In the Developer Console, click **"Add new app"** (or the **`+`** button).
   - Select platform: **Android**.

3. **Upload the Release APK**:
   - Upload file: [`app/build/outputs/apk/release/app-release.apk`](file:///d:/RSET/PROJECT/S%20Secure/app/build/outputs/apk/release/app-release.apk).
   - Uptodown will automatically parse package name `com.lockeyy.applock`, version `1.0.0`, and target SDK `35`.

4. **Fill In Metadata**:
   - **App Name**: `Lockeyy - App Lock & Privacy Shield`
   - **Category**: `Tools` / `Security`
   - **License**: `Free`
   - **Short Description**: `Fast, sleek app locker with biometric, PIN & pattern security.`
   - **Full Description**: Copy & paste the text from [Section 2: Store Listing Content](#-2-store-listing-content-copy--paste).
   - **Privacy Policy**: Paste your GitHub Gist URL from [Section 4: Privacy Policy URL](#-4-privacy-policy-url).

5. **Upload Media & Graphics**:
   - **App Icon**: Upload [`store_assets/icon_512x512.png`](file:///d:/RSET/PROJECT/S%20Secure/store_assets/icon_512x512.png) (512x512 high-resolution square PNG).
   - **Screenshots**: Upload 2 to 4 screenshots of the app (Lock screen, App selection screen, Settings screen).

6. **Submit for Review**:
   - Click **Submit** / **Send for review**.
   - **Review Timeline**: 24 – 48 hours. The Uptodown editorial team and automated VirusTotal scanners will verify the app.
   - Once approved, the app status will transition to **Published** and appear on Uptodown.com and the Uptodown Android App Store!

