import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.davidepianca98.socket.ConnectionDetails
import io.github.davidepianca98.socket.SocketProtocolType
import kotlin.time.ExperimentalTime


@OptIn(ExperimentalUnsignedTypes::class, ExperimentalMaterialApi::class)
@Composable
fun App(vm: AppViewModel = AppViewModel()) {
    val scope = rememberCoroutineScope()
    val state by vm.uiState.collectAsState()

    Scaffold { innerPadding ->
        Row(Modifier.padding(innerPadding), horizontalArrangement = Arrangement.spacedBy(10.0.dp)) {
            Column(modifier = Modifier.padding(16.dp).weight(1f)) {
                connectionDetails(state.connectionDetails) {
                    vm.onAction(UiAction.UpdateConnectionDetails(it))
                }

                connectionStatus(
                    modifier = Modifier.align(Alignment.End),
                    state.isConnected,
                    connect = { vm.onAction(UiAction.Connect) },
                    disconnect = { vm.onAction(UiAction.Disconnect) })

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    state.message,
                    enabled = state.isConnected,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Message") },
                    onValueChange = { vm.onAction(UiAction.UpdateMessage(it)) })

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        state.sendOnTopic,
                        enabled = state.isConnected,
                        modifier = Modifier.width(300.dp),
                        label = { Text("Topic") },
                        onValueChange = { vm.onAction(UiAction.UpdateTopic(it)) })

                    Button(
                        enabled = state.isConnected && state.sendOnTopic.isNotEmpty() && state.message.isNotEmpty(),
                        onClick = {
                            vm.onAction(UiAction.SendMessage)
                        }
                    ) {
                        Text("Publish")
                    }
                }
            }

            Column(
                modifier = Modifier.weight(2f, true).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.0.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.0.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        state.subscribeToTopic,
                        modifier = Modifier.width(300.dp),
                        label = { Text("Subscribe to topic") },
                        onValueChange = { vm.onAction(UiAction.UpdateSubscribedTopic(it)) })

                    Button(onClick = {
                        vm.onAction(UiAction.Subscribe)
                    }) {
                        Text("Add subscription")
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
                }
                val listState = rememberLazyListState()

                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f).fillMaxWidth().border(1.dp, Color.Gray, RoundedCornerShape(10.dp))
                ) {
                    items(state.incomingAndOutgoingMessages) {
                        mqttMessageView(it)
                    }
                }

                //Scroll to the bottom of the list when a new message is received
                LaunchedEffect(state.incomingAndOutgoingMessages.size) {
                    if (state.incomingAndOutgoingMessages.isNotEmpty()) {
                        listState.scrollToItem(state.incomingAndOutgoingMessages.size - 1)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalUnsignedTypes::class, ExperimentalTime::class)
@Composable
fun mqttMessageView(messages: IncomingOutgoing, modifier: Modifier = Modifier) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(10.dp),
        horizontalArrangement = if (messages is Outgoing) Arrangement.End else Arrangement.Start
    ) {
        Card(modifier = modifier.padding(10.dp)) {
            Column(
                modifier = Modifier.background(if (messages is Outgoing) Color.Green else Color.LightGray)
                    .padding(10.dp), verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text("Payload: ${messages.message}")
                Text("Topic: " + messages.topic)
                Text("QOS: " + messages.qos)
                Text("Timestamp: " + messages.timestamp)
            }
        }
    }
}

@Composable
fun ColumnScope.connectionDetails(connectionDetails: ConnectionDetails, update: (ConnectionDetails) -> Unit) {
    Text(
        "Connection details",
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.align(Alignment.Start).padding(bottom = 10.dp)
    )

    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        ExposedDropdownMenuBox(
            connectionDetails.protocol.shortName(),
            modifier = Modifier.padding(top = 8.dp).widthIn(max = 120.dp).height(50.dp),
            options = SocketProtocolType.entries.map { it.name }) {
            update(connectionDetails.copy(protocol = SocketProtocolType.valueOf(it)))
        }

        connectionDetails.address.outlineTextFieldValidation(
            "Address",
            modifier = Modifier.widthIn(max = 200.dp),
            validate = { this.ifEmpty { throw Exception("Address cannot be empty") } }) {
            update(connectionDetails.copy(address = it))
        }

        connectionDetails.port.outlineTextFieldValidation(
            "Port", modifier = Modifier.widthIn(max = 80.dp), validate = { toInt() }) {
            update(connectionDetails.copy(port = it))
        }
    }
}

@Composable
fun connectionStatus(modifier: Modifier, isConnected: Boolean, connect: () -> Unit, disconnect: () -> Unit) {
    Column(modifier) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = disconnect, enabled = isConnected) { Text("Disconnect") }
            Button(onClick = connect, enabled = !isConnected) { Text("Connect") }
        }

        Text("Connection status: $isConnected", modifier = Modifier.align(Alignment.CenterHorizontally))
    }
}
