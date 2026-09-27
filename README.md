# The Chatters

A modern, fast, and secure Android messaging and collaboration application built with Jetpack Compose, Material 3, Room Database, and Gemini AI.

## Features

- **Chats & Messaging**: Real-time direct conversations with message history.
- **Groups**: Create and manage group discussions.
- **Status Updates**: Share text and media status updates with your contacts.
- **Voice & Video Calls**: Clean calling UI with call history and status.
- **Brain AI**: Built-in intelligent assistant powered by Gemini.
- **Sticker Maker & Media Sharing**: Custom sticker creation and media tools.

---

## GitHub Actions CI/CD Build Setup

The repository includes automated CI/CD via GitHub Actions (`.github/workflows/build.yml`) that automatically compiles both debug APK and bundle artifacts on every push to `main`.

### Default Build (No Secrets Required)
The build workflow is fully automated to succeed out-of-the-box without requiring any repository secrets. In the absence of secrets, the build dynamically generates safe signing keys for testing.

---

## Required GitHub Secrets for Release Builds

To sign release builds (AAB for Google Play Store and production APK), configure the following repository secrets under **GitHub Repository Settings** &rarr; **Secrets and variables** &rarr; **Actions**:

| Secret Name | Description | Example |
| :--- | :--- | :--- |
| `KEYSTORE_BASE64` | Base64-encoded upload keystore (`my-upload-key.jks`) | `base64 -w 0 my-upload-key.jks` |
| `KEYSTORE_PASSWORD` | Password protecting the keystore file | `your_keystore_password` |
| `KEY_ALIAS` | Key alias in the keystore | `upload` |
| `KEY_PASSWORD` | Password protecting the key alias | `your_key_password` |

### How to Generate `KEYSTORE_BASE64` Locally
```bash
# On Linux / macOS:
base64 -w 0 my-upload-key.jks

# On macOS (BSD base64):
base64 -i my-upload-key.jks | tr -d '\n'
```
Paste the output string as the value for the `KEYSTORE_BASE64` secret.
