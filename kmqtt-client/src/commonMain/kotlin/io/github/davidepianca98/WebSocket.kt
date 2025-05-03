package io.github.davidepianca98

import io.github.davidepianca98.socket.ConnectionDetails
import io.github.davidepianca98.socket.SocketInterface
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

public class WebSocket(private val connectionDetails: ConnectionDetails) : SocketInterface {

    //TODO: handle exception properly inside of scope
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default).apply {
    }
    private var session: WebSocketSession? = null

    private val client = HttpClient() {
        install(WebSockets)
    }

    public override suspend fun connect() {
        session = client.webSocketSession(connectionDetails.toUrl()) {
            header(HttpHeaders.SecWebSocketProtocol, "mqtt")
        }

        println("Connected to WebSocket")
    }

    override suspend fun send(data: UByteArray) {
        session?.send(Frame.Binary(true, data.toByteArray()))
    }

    override fun sendRemaining() {
        //NO-OP, we let send do the work for us
    }

    override suspend fun read(): UByteArray? {
        val frame = session?.incoming?.receive() ?: return null
        println("Received frame of type ${frame.frameType}")

        return when (frame) {
            is Frame.Binary -> return frame.data.asUByteArray()
            else -> null
        }
    }

    override fun close() {
        scope.launch {
            session?.close()
        }
    }
}
