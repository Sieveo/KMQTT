package io.github.davidepianca98.socket

public interface SocketInterface {
    public suspend fun connect()

    public suspend fun send(data: UByteArray)

    public fun sendRemaining()

    public suspend fun read(): UByteArray?

    public fun close()
}

public enum class SocketProtocolType {
    TCP_SOCKET, TLS_SOCKET, WEB_SOCKET, SECURE_WEB_SOCKET;

    public fun toProtocolName(): String = when (this) {
        TCP_SOCKET -> "tcp"
        TLS_SOCKET -> "tls"
        WEB_SOCKET -> "ws"
        SECURE_WEB_SOCKET -> "wss"
    }
}

public data class ConnectionDetails(
    val address: String,
    val port: Int,
    val protocol: SocketProtocolType,
    val path: String = "/mqtt"
) {

    public fun toUrl(): String = "${protocol.toProtocolName()}://$address:$port$path"

    public companion object {
        public val localhostWS: ConnectionDetails = ConnectionDetails("127.0.0.1", 1884, SocketProtocolType.WEB_SOCKET)
        public val localhostWSS: ConnectionDetails =
            ConnectionDetails("127.0.0.1", 1884, SocketProtocolType.SECURE_WEB_SOCKET)
        public val localhostTCP: ConnectionDetails = ConnectionDetails("127.0.0.1", 1884, SocketProtocolType.TCP_SOCKET)
    }
}