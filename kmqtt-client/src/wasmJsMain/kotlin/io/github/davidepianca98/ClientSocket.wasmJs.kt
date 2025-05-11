package io.github.davidepianca98

import io.github.davidepianca98.socket.ConnectionDetails
import io.github.davidepianca98.socket.SocketInterface

public actual fun createClientSocket(connectionDetails: ConnectionDetails): SocketInterface {
    throw UnsupportedOperationException("TCP Sockets are not supported on WASM JS.")
}