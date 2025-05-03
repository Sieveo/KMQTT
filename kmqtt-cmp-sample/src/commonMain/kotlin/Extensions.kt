import io.github.davidepianca98.socket.SocketProtocolType
import io.github.davidepianca98.socket.SocketProtocolType.SECURE_WEB_SOCKET
import io.github.davidepianca98.socket.SocketProtocolType.TCP_SOCKET
import io.github.davidepianca98.socket.SocketProtocolType.TLS_SOCKET
import io.github.davidepianca98.socket.SocketProtocolType.WEB_SOCKET


fun SocketProtocolType.shortName(): String = when (this) {
    TCP_SOCKET -> "TCP"
    TLS_SOCKET -> "TLS"
    WEB_SOCKET -> "WS "
    SECURE_WEB_SOCKET -> "WSS"
}