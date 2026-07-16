# cordova-plugin-firebase-salesforce-bridge

Cordova extension plugin that acts as a bridge between the official Firebase Messaging plugin (`cordova-plugin-firebase-messaging`) and the Salesforce Marketing Cloud SDK on Android.

This plugin allows keeping the official OutSystems Firebase Messaging repository unmodified in your mobile applications, isolating all Salesforce Marketing Cloud SDK custom integrations in a clean, maintainable way.

---

## How It Works

### 1. Android (Automatic Interception of Push Notifications & Tokens)
Firebase Cloud Messaging (FCM) only allows a single service to handle incoming messages and token changes.

This extension plugin:
* Uses Android Manifest merging (`tools:node="remove"`) to remove the default `FirebaseMessagingPluginService` registered by the official plugin.
* Registers a custom service `SalesforceFCMService` that extends `FirebaseMessagingPluginService`.
* Intercepts all incoming messages. If the message originates from Salesforce (`PushMessageManager.isMarketingCloudPush(remoteMessage)`), it is handled by the Salesforce SDK (`MarketingCloudSdk.handleMessage()`).
* Otherwise, it delegates message processing and token updates to the official native service (`super.onMessageReceived()` / `super.onNewToken()`), ensuring default Firebase behavior (such as Javascript events) continues to work.

### 2. Android (Startup Token Synchronization)
At application startup, the native plugin proactively requests the current token from Firebase and sends it to the Salesforce Marketing Cloud SDK, preventing any token mismatch.

### 3. iOS (Salesforce SDK Native Behavior)
No code modifications or manual bridging are required on iOS. The native Salesforce Marketing Cloud SDK for iOS uses *Method Swizzling* on the `AppDelegate` to automatically intercept token registration and background push notifications.

---

## Prerequisites

1. The official **`cordova-plugin-firebase-messaging`** plugin must be installed.
2. The **Salesforce Marketing Cloud SDK** must be integrated and initialized in the application (either through another Salesforce plugin or native configuration). The class paths `com.salesforce.marketingcloud.MarketingCloudSdk` must be available during Android compilation.

---

## Installation

Add the plugin to your Cordova/OutSystems project using the Git repository URL:

```bash
cordova plugin add cordova-plugin-firebase-salesforce-bridge
```

In **OutSystems Integration Studio** or in your JSON configuration model, add the Git dependency to your repository:
```json
{
    "plugin": {
        "url": "https://github.com/MiguelRosaDev/cordova-plugin-firebase-salesforce-bridge.git"
    }
}
```

---

## License

MIT License.
