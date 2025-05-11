package io.github.davidepianca98

import io.github.davidepianca98.socket.ConnectionDetails
import io.github.davidepianca98.socket.SocketInterface

public expect fun createClientSocket(connectionDetails: ConnectionDetails): SocketInterface