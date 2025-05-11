import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.davidepianca98.MQTTClient
import io.github.davidepianca98.mqtt.Subscription
import io.github.davidepianca98.mqtt.packets.Qos
import io.github.davidepianca98.mqtt.packets.mqtt.MQTTPublish
import io.github.davidepianca98.mqtt.packets.mqttv5.ReasonCode
import io.github.davidepianca98.socket.ConnectionDetails
import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

sealed interface IncomingOutgoing {
    val message: String
    val topic: String
    val qos: String

    @OptIn(ExperimentalTime::class)
    val timestamp: Instant
}

class Incoming(mqttPublish: MQTTPublish) : IncomingOutgoing {
    @OptIn(ExperimentalUnsignedTypes::class)
    override val message: String = mqttPublish.payload?.toByteArray()?.decodeToString() ?: ""
    override val topic: String = mqttPublish.topicName
    override val qos: String = mqttPublish.qos.toString()

    @OptIn(ExperimentalTime::class)
    override val timestamp: Instant = Instant.fromEpochMilliseconds(mqttPublish.timestamp)
}

class Outgoing(override val message: String, override val topic: String, override val qos: String) : IncomingOutgoing {
    @OptIn(ExperimentalTime::class)
    override val timestamp: Instant = Clock.System.now()
}

@OptIn(ExperimentalUnsignedTypes::class)
class AppViewModel : ViewModel() {
    data class UiState(
        val connectionDetails: ConnectionDetails = ConnectionDetails.localhostWS,
        val isConnected: Boolean = false,
        val message: String = "",
        val sendOnTopic: String = "",
        val subscribeToTopic: String = "",
        val incomingAndOutgoingMessages: List<IncomingOutgoing> = emptyList(),
        val subscriptions: List<Subscription> = emptyList()
    )

    val uiState = MutableStateFlow(UiState())
    var client: MQTTClient = MQTTClient(
        connectionDetails = uiState.value.connectionDetails, publishReceived = {
            uiState.value = uiState.value.copy(
                incomingAndOutgoingMessages = uiState.value.incomingAndOutgoingMessages + Incoming(it)
            )
        })

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

            is UiAction.Connect -> connect()
            is UiAction.Disconnect -> disconnect()
        }
    }

    private fun connect() {
        client = MQTTClient(
            connectionDetails = uiState.value.connectionDetails,
            publishReceived = {
                uiState.value = uiState.value.copy(
                    incomingAndOutgoingMessages = uiState.value.incomingAndOutgoingMessages + Incoming(it)
                )
            },
            onConnected = { uiState.value = uiState.value.copy(isConnected = true) },
            onDisconnected = { uiState.value = uiState.value.copy(isConnected = false) })

        viewModelScope.launch {
            client.run(this)
        }
    }

    private suspend fun disconnect() {
        client.disconnect(ReasonCode.SUCCESS)
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
        uiState.value = uiState.value.copy(
            incomingAndOutgoingMessages = uiState.value.incomingAndOutgoingMessages + Outgoing(
                uiState.value.message,
                uiState.value.sendOnTopic,
                Qos.EXACTLY_ONCE.toString()
            )
        )
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
    data object Connect : UiAction()
    data object Disconnect : UiAction()
}
