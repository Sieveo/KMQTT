package io.github.davidepianca98.socket

public interface SocketInterface {
    public suspend fun connect()

    public suspend fun send(data: UByteArray)

    public fun sendRemaining()

    public suspend fun read(): UByteArray?

    public fun close()
}

public enum class SocketProtocolType{
    TCP_SOCKET, TLS_SOCKET, WEB_SOCKET, SECURE_WEB_SOCKET;
}