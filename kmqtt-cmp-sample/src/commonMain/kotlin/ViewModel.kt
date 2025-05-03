import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.davidepianca98.MQTTClient
import io.github.davidepianca98.mqtt.Subscription
import io.github.davidepianca98.mqtt.packets.Qos
import io.github.davidepianca98.mqtt.packets.mqtt.MQTTPublish
import io.github.davidepianca98.socket.SocketProtocolType
import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

data class ConnectionDetails(
    val address: String,
    val port: Int,
    val protocol: SocketProtocolType
) {
    companion object {
        val localhostWS = ConnectionDetails("127.0.0.1", 1884, SocketProtocolType.WEB_SOCKET)
    }
}

@OptIn(ExperimentalUnsignedTypes::class)
class AppViewModel : ViewModel() {
    data class UiState(
        val connectionDetails: ConnectionDetails = ConnectionDetails.localhostWS,
        val message: String = "",
        val sendOnTopic: String = "",
        val subscribeToTopic: String = "",
        val receivedMessages: List<MQTTPublish> = emptyList(),
        val subscriptions: List<Subscription> = emptyList()
    )

    val uiState = MutableStateFlow(UiState())

    fun onAction(action: UiAction) = viewModelScope.launch {
        when (action) {
            is UiAction.SendMessage -> sendMessage()
            is UiAction.Subscribe -> subscribe(uiState.value.subscribeToTopic)
            is UiAction.UnSubscribe -> unsubscribe(action.subscription)
            is UiAction.UpdateMessage -> uiState.value = uiState.value.copy(message = action.message)
            is UiAction.UpdateTopic -> uiState.value = uiState.value.copy(sendOnTopic = action.topic)

            is UiAction.UpdateSubscribedTopic -> uiState.value =
                uiState.value.copy(subscribeToTopic = action.subscribedTopic)

            is UiAction.UpdateConnectionDetails -> uiState.value =
                uiState.value.copy(connectionDetails = action.connectionDetails)

        }
    }

    private suspend fun subscribe(topic: String) {
        val subscription = Subscription(topic)
        uiState.value = uiState.value.copy(subscriptions = uiState.value.subscriptions + subscription)
        client.subscribe(listOf(subscription))
    }

    private suspend fun unsubscribe(subscription: Subscription) {
        uiState.value = uiState.value.copy(subscriptions = uiState.value.subscriptions.filterNot { it == subscription })
        client.unsubscribe(listOf(subscription.topicFilter))
    }

    private suspend fun sendMessage() {
        client.publish(
            retain = false,
            qos = Qos.EXACTLY_ONCE,
            topic = uiState.value.sendOnTopic,
            payload = uiState.value.message.toByteArray().toUByteArray()
        )
    }

    val client: MQTTClient = MQTTClient(address = "127.0.0.1", port = 1884, publishReceived = {
        uiState.value = uiState.value.copy(receivedMessages = uiState.value.receivedMessages + it)
    })

    init {
        viewModelScope.launch {
            client.run(this)
        }
    }
}

sealed class UiAction {
    data object SendMessage : UiAction()
    data object Subscribe : UiAction()
    data class UnSubscribe(val subscription: Subscription) : UiAction()
    data class UpdateMessage(val message: String) : UiAction()
    data class UpdateTopic(val topic: String) : UiAction()
    data class UpdateSubscribedTopic(val subscribedTopic: String) : UiAction()
    data class UpdateConnectionDetails(val connectionDetails: ConnectionDetails) : UiAction()
}
