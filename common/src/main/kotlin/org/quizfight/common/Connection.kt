package org.quizfight.common

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.quizfight.common.messages.Message
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.ServerSocket
import java.net.Socket
import kotlin.reflect.KClass

interface Connection {
    fun send(msg: Message)
    val handlers: Map<KClass<*>, (Connection, Message) -> Unit>
}

class SocketConnection(
    val socket: Socket,
    override val handlers: Map<KClass<*>, (Connection, Message) -> Unit>
) : Connection {
    // Do not switch order of stream creation, will result in deadlock
    private val outStream = ObjectOutputStream(socket.getOutputStream())
    private val inStream  = ObjectInputStream(socket.getInputStream())

    init {
        /* TODO: Type check handler map
            for (key in handler.keys)
            if (Message::class in key.supertypes)
         */
        receiveAsync()
    }

    private fun receiveAsync() = GlobalScope.launch {
        while (socket.isConnected) {
            val msg = inStream.readObject() as? Message ?: throw Exception("Received invalid object")
            val handler = handlers[msg::class] ?: throw Exception("No handler found for message type ${msg::class}")
            handler(this@SocketConnection, msg)
        }
    }

    override fun send(msg: Message) {
        outStream.writeObject(msg)
    }
}