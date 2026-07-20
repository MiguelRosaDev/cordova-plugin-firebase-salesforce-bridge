package com.salesforce.cordova.firebase;

import androidx.annotation.NonNull;
import com.google.firebase.messaging.RemoteMessage;
import com.salesforce.marketingcloud.MarketingCloudSdk;
import com.salesforce.marketingcloud.messages.push.PushMessageManager;

public class SalesforceFCMService extends com.outsystems.firebase.messaging.FirebaseMessagingReceiveService {

    @Override
    public void onNewToken(@NonNull String token) {
        // Envia o token para o plugin oficial (permitindo callbacks no JS)
        super.onNewToken(token);
        
        // Envia o token para o Salesforce SDK
        MarketingCloudSdk.requestSdk(sdk -> sdk.getPushMessageManager().setPushToken(token));
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        // Se for uma notificação push do Salesforce, deixa o SDK do Salesforce processá-la
        if (PushMessageManager.isMarketingCloudPush(remoteMessage)) {
            MarketingCloudSdk.requestSdk(sdk -> sdk.getPushMessageManager().handleMessage(remoteMessage));
        } else {
            // Caso contrário, passa para a lógica padrão do plugin de Firebase oficial
            super.onMessageReceived(remoteMessage);
        }
    }
}
