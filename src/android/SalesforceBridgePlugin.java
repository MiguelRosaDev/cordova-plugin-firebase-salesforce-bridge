package com.salesforce.cordova.firebase;

import org.apache.cordova.CordovaPlugin;
import com.google.firebase.messaging.FirebaseMessaging;
import com.salesforce.marketingcloud.MarketingCloudSdk;

public class SalesforceBridgePlugin extends CordovaPlugin {

    @Override
    protected void pluginInitialize() {
        super.pluginInitialize();
        
        // Garante que o token atual (cached) é passado ao Salesforce no arranque da aplicação
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                String token = task.getResult();
                MarketingCloudSdk.requestSdk(sdk -> sdk.getPushMessageManager().setPushToken(token));
            }
        });
    }
}
