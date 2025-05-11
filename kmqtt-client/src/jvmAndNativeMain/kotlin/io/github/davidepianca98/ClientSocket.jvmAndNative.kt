package io.github.davidepianca98

import io.github.davidepianca98.socket.ConnectionDetails
import io.github.davidepianca98.socket.SocketClosedException
import io.github.davidepianca98.socket.SocketInterface
import io.github.davidepianca98.socket.SocketProtocolType
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.network.tls.*
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.availableForRead
import io.ktor.utils.io.cancel
import io.ktor.utils.io.close
import io.ktor.utils.io.core.readBytes
import io.ktor.utils.io.read
import io.ktor.utils.io.readByteArray
import io.ktor.utils.io.readPacket
import io.ktor.utils.io.readUntil
import io.ktor.utils.io.write
import io.ktor.utils.io.writeByteArray
import io.ktor.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.io.readByteArray

public actual fun createClientSocket(connectionDetails: ConnectionDetails): SocketInterface {
    return TcpSocket(connectionDetails)
}

public class TcpSocket(private val connectionDetails: ConnectionDetails) : SocketInterface {
    //TODO: handle exception properly inside of scope
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default).apply {
    }

    private val selectorManager = SelectorManager(Dispatchers.IO)
    private var socket: Socket? = null
    private var receiveChannel: ByteReadChannel? = null
    private var sendChannel: ByteWriteChannel? = null

    public override suspend fun connect() {
        socket = aSocket(selectorManager)
            .tcp()
            .connect(connectionDetails.address, connectionDetails.port)
            .apply {
                if (connectionDetails.protocol == SocketProtocolType.TLS_SOCKET) {
                    this.tls(Dispatchers.IO)
                }
            }

        receiveChannel = socket?.openReadChannel()
        sendChannel = socket?.openWriteChannel(autoFlush = true)

        println("Connected to TCP Socket")
    }

    override suspend fun send(data: UByteArray) {
        sendChannel?.writeByteArray(data.toByteArray()) ?: throw SocketClosedException("TCP Socket send failed")
    }

    override fun sendRemaining() {
        //NO-OP, we let send do the work for us
    }

    override suspend fun read(): UByteArray? {
        val available = receiveChannel?.availableForRead ?: throw SocketClosedException("TCP Socket read failed")
        val bytesRead = receiveChannel?.readByteArray(available)?.asUByteArray() ?: return null
        return bytesRead
    }

    override fun close() {
        scope.launch {
            receiveChannel?.cancel()
            sendChannel?.flushAndClose()
            socket?.close()
        }
    }
}

