import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.davidepianca98.mqtt.packets.mqtt.MQTTPublish
import io.github.davidepianca98.socket.SocketProtocolType
import kotlin.time.ExperimentalTime
import kotlin.time.Instant


@OptIn(ExperimentalUnsignedTypes::class, ExperimentalTime::class)
@Composable
fun mqttMessageView(messages: MQTTPublish) {
    Card(modifier = Modifier.padding(10.dp)) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text("Payload: ${messages.payload?.toByteArray()?.decodeToString()}")
            Text("Topic: " + messages.topicName)
            Text("QOS: " + messages.qos.toString())

            Text("Timestamp: " + Instant.fromEpochMilliseconds(messages.timestamp).toString())
        }
    }
}

@Composable
fun connectionDetails(connectionDetails: ConnectionDetails, update: (ConnectionDetails) -> Unit) =
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        ExposedDropdownMenuBox(
            "Protocol:\n${connectionDetails.protocol}",
            modifier = Modifier.width(250.dp),
            options = SocketProtocolType.entries.map { it.name }) {
            update(connectionDetails.copy(protocol = SocketProtocolType.valueOf(it)))
        }

        connectionDetails.address.outlineTextFieldValidation(
            "Address",
            validate = { this.ifEmpty { throw Exception("Address cannot be empty") } }) {
            update(connectionDetails.copy(address = it))
        }

        connectionDetails.port.outlineTextFieldValidation(
            "Port",
            modifier = Modifier.width(80.dp),
            validate = { toInt() }) {
            update(connectionDetails.copy(port = it))
        }
    }

@OptIn(ExperimentalUnsignedTypes::class, ExperimentalMaterialApi::class)
@Composable
fun App(vm: AppViewModel = AppViewModel()) {
    val scope = rememberCoroutineScope()
    val state by vm.uiState.collectAsState()

    Scaffold { innerPadding ->
        Row(Modifier.padding(innerPadding), horizontalArrangement = Arrangement.spacedBy(10.0.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.0.dp)) {
                connectionDetails(state.connectionDetails) {
                    vm.onAction(UiAction.UpdateConnectionDetails(it))
                }
                OutlinedTextField(
                    state.message,
                    modifier = Modifier.width(300.dp),
                    label = { Text("Message") },
                    onValueChange = { vm.onAction(UiAction.UpdateMessage(it)) })
                OutlinedTextField(
                    state.sendOnTopic,
                    modifier = Modifier.width(300.dp),
                    label = { Text("Topic") },
                    onValueChange = { vm.onAction(UiAction.UpdateTopic(it)) })

                Button(onClick = {
                    vm.onAction(UiAction.SendMessage)
                }) {
                    Text("Publish")
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.0.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.0.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        state.subscribeToTopic,
                        modifier = Modifier.width(300.dp),
                        label = { Text("Subscribe to topic:") },
                        onValueChange = { vm.onAction(UiAction.UpdateSubscribedTopic(it)) })

                    Button(onClick = {
                        vm.onAction(UiAction.Subscribe)
                    }) {
                        Text("Add subscription")
                    }
                }

                FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.0.dp)) {
                    Text("Subscriptions: ", modifier = Modifier.padding(10.dp))
                    state.subscriptions.forEach {
                        InputChip(
                            selected = true,
                            onClick = { vm.onAction(UiAction.UnSubscribe(it)) },
                            label = { Text(it.topicFilter) },
                            trailingIcon = { Icon(Icons.Default.Close, contentDescription = "Close") })
                    }
                }

                LazyColumn(
                    Modifier.weight(1f).fillMaxWidth()
                        .border(1.dp, Color.Gray, RoundedCornerShape(10.dp))
                ) {
                    items(state.receivedMessages) { mqttMessageView(it) }
                }
            }
        }
    }
}

