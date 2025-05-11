package io.github.davidepianca98.socket.tcp

import io.github.davidepianca98.socket.OldSocketInterface


public expect open class Socket : OldSocketInterface {

    override fun send(data: UByteArray)

    override fun sendRemaining()

    override fun read(): UByteArray?

    override fun close()
}