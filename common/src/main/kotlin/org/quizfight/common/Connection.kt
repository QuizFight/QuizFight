package org.quizfight.common

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.quizfight.common.messages.Message
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.Socket
import kotlin.reflect.KClass

interface Connection {
    fun send(msg: Message)
    fun close()
    fun withHandlers(handlers: Map<KClass<*>, (Connection, Message) -> Unit>): Connection
    val handlers: Map<KClass<*>, (Connection, Message) -> Unit>
}

class SocketConnection(
    private val socket: Socket,
    override val handlers: Map<KClass<*>, (Connection, Message) -> Unit>
) : Connection {
    // Do not switch order of stream creation, will result in deadlock
    private val outStream = ObjectOutputStream(socket.getOutputStream())
    private val inStream  = ObjectInputStream(socket.getInputStream())
    // Flag to stop handling messages when withHandlers is called
    private var handleMessages = true

    init {
        /* TODO: Type check handler map
            for (key in handler.keys)
            if (Message::class in key.supertypes)
         */
        receiveAsync()
    }

    private fun receiveAsync() = GlobalScope.launch {
        while (!socket.isClosed && handleMessages) {
            val msg = inStream.readObject() as? Message ?: throw Exception("Received invalid object")
            val handler = handlers[msg::class] ?: throw Exception("No handler found for message type ${msg::class}")
            handler(this@SocketConnection, msg)
        }
    }

    override fun send(msg: Message) {
        outStream.writeObject(msg)
    }

    override fun close() {
        socket.close()
    }

    override fun withHandlers(handlers: Map<KClass<*>, (Connection, Message) -> Unit>): Connection {
        // TODO: This should probably kill the coroutine, too, in case it's already blocking on readObject.
        // Note: This should only kill the coroutine if this method is not executed within the coroutine!
        //       Otherwise, it suicides and the new Connection is never created.
        handleMessages = false
        return SocketConnection(socket, handlers)
    }
}
