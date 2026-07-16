# cordova-plugin-firebase-salesforce-bridge

Plugin Cordova de extensão que faz a ponte entre o plugin oficial de Firebase Messaging (**`cordova-plugin-firebase-messaging`**) e o **Salesforce Marketing Cloud SDK** (no Android).

Este plugin permite manter o repositório oficial do Firebase Messaging intocado nas tuas aplicações OutSystems, encapsulando toda a customização do Salesforce Marketing Cloud SDK de forma isolada e limpa.

---

## Como Funciona

### 1. Android (Interceção Automática de Push e Tokens)
O Firebase Cloud Messaging (FCM) apenas permite que um único serviço trate as mensagens e tokens recebidos. 

Este plugin de extensão:
* Utiliza a fusão de manifesto Android (`tools:node="remove"`) para remover a declaração do serviço padrão `FirebaseMessagingPluginService` registado pelo plugin oficial.
* Regista o serviço personalizado `SalesforceFCMService` que herda diretamente de `FirebaseMessagingPluginService`.
* Intercepta todas as mensagens de entrada. Se a mensagem for originária do Salesforce (`PushMessageManager.isMarketingCloudPush(remoteMessage)`), é tratada pelo SDK do Salesforce (`MarketingCloudSdk.handleMessage()`).
* Caso contrário, delega o processamento da mensagem e o registo de tokens para o comportamento nativo oficial (`super.onMessageReceived()` / `super.onNewToken()`), garantindo que o comportamento padrão do Firebase (eventos de JS, etc.) continua a funcionar perfeitamente.

### 2. Android (Sincronização de Tokens no Arranque)
No arranque da aplicação, o plugin nativo solicita proativamente o token atual (cached) ao Firebase e envia-o imediatamente para o Salesforce Marketing Cloud SDK, prevenindo qualquer dessincronização de tokens.

### 3. iOS (Comportamento Nativo do Salesforce SDK)
Não é necessária qualquer alteração de código ou bridging manual no iOS. O SDK nativo do Salesforce Marketing Cloud para iOS utiliza *Method Swizzling* no `AppDelegate` para intercetar o registo de tokens e receção de notificações em background automaticamente.

---

## Requisitos Prévios

1. O plugin oficial **`cordova-plugin-firebase-messaging`** (preferencialmente a versão oficial da OutSystems ou upstream estável) deve estar instalado.
2. O **Salesforce Marketing Cloud SDK** deve estar integrado e inicializado na aplicação (seja através de outro plugin do Salesforce ou de configuração nativa). Os caminhos de classes `com.salesforce.marketingcloud.MarketingCloudSdk` devem estar disponíveis durante a compilação do Android.

---

## Instalação

Adiciona o plugin ao teu projeto Cordova/OutSystems utilizando o URL do repositório Git ou o nome local:

```bash
cordova plugin add cordova-plugin-firebase-salesforce-bridge
```

No **OutSystems Integration Studio** ou no teu modelo de configuração JSON, adiciona a dependência Git ao teu repositório:
```json
{
    "plugin": {
        "url": "https://github.com/MiguelRosaDev/cordova-plugin-firebase-salesforce-bridge.git"
    }
}
```

---

## Licença

MIT License.
