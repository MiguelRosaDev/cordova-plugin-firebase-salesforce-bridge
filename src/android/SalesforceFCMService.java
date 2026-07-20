package com.salesforce.cordova.firebase;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.salesforce.marketingcloud.MarketingCloudSdk;
import com.salesforce.marketingcloud.messages.push.PushMessageManager;

public class SalesforceFCMService extends FirebaseMessagingService {
    private static final String TAG = "SalesforceFCMBridge";
    private FirebaseMessagingService outsystemsDelegate;
    private boolean delegateInitialized = false;

    private synchronized void initializeDelegate() {
        if (delegateInitialized) return;
        
        // List of possible package classes for the base OutSystems Firebase service
        String[] possibleClasses = {
            "com.outsystems.plugins.firebase.messaging.FirebaseMessagingReceiveService",
            "com.outsystems.firebase.messaging.FirebaseMessagingReceiveService",
            "com.outsystems.firebase.cloudmessaging.FirebaseMessagingReceiveService",
            "by.chemerisuk.cordova.firebase.FirebaseMessagingPluginService"
        };
        
        for (String className : possibleClasses) {
            try {
                Class<?> clazz = Class.forName(className);
                outsystemsDelegate = (FirebaseMessagingService) clazz.getDeclaredConstructor().newInstance();
                
                // Inject the application context into the delegate service wrapper
                java.lang.reflect.Method attachMethod = Class.forName("android.content.ContextWrapper")
                    .getDeclaredMethod("attachBaseContext", Context.class);
                attachMethod.setAccessible(true);
                attachMethod.invoke(outsystemsDelegate, getApplicationContext());
                
                // Initialize the delegate's lifecycle
                outsystemsDelegate.onCreate();
                
                Log.d(TAG, "Successfully loaded and initialized delegate: " + className);
                break;
            } catch (ClassNotFoundException e) {
                Log.d(TAG, "Class not found: " + className);
            } catch (Exception e) {
                Log.e(TAG, "Failed to initialize delegate: " + className, e);
            }
        }
        delegateInitialized = true;
    }

    @Override
    public void onNewToken(@NonNull String token) {
        // 1. Forward the token to the Salesforce Marketing Cloud SDK
        try {
            MarketingCloudSdk.requestSdk(sdk -> sdk.getPushMessageManager().setPushToken(token));
        } catch (Exception e) {
            Log.e(TAG, "Failed to send token to Marketing Cloud SDK", e);
        }
        
        // 2. Forward the token to the underlying OutSystems plugin
        initializeDelegate();
        if (outsystemsDelegate != null) {
            try {
                outsystemsDelegate.onNewToken(token);
            } catch (Exception e) {
                Log.e(TAG, "Error forwarding onNewToken to delegate", e);
            }
        }
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        // 1. If it's a Salesforce push notification, process it using the Salesforce SDK
        if (PushMessageManager.isMarketingCloudPush(remoteMessage)) {
            try {
                MarketingCloudSdk.requestSdk(sdk -> sdk.getPushMessageManager().handleMessage(remoteMessage));
            } catch (Exception e) {
                Log.e(TAG, "Failed to pass push to Marketing Cloud SDK", e);
            }
        } else {
            // 2. Otherwise, forward it to the official OutSystems FCM plugin
            initializeDelegate();
            if (outsystemsDelegate != null) {
                try {
                    outsystemsDelegate.onMessageReceived(remoteMessage);
                } catch (Exception e) {
                    Log.e(TAG, "Error forwarding onMessageReceived to delegate", e);
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        if (outsystemsDelegate != null) {
            try {
                outsystemsDelegate.onDestroy();
            } catch (Exception e) {
                Log.e(TAG, "Error destroying delegate", e);
            }
        }
        super.onDestroy();
    }
}
