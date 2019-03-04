package org.quizfight.common

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.quizfight.common.messages.Message
import java.io.EOFException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.Socket
import java.net.SocketException
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.reflect.KClass

interface Connection {
    fun send(msg: Message)
    fun close()
    fun withHandlers(handlers: Map<KClass<*>, (Connection, Message) -> Unit>): Connection
    var handlers: Map<KClass<*>, (Connection, Message) -> Unit>

    //TEMPORARY
    val id: String
}

class SocketConnection(
    private val socket: Socket,
    override var handlers: Map<KClass<*>, (Connection, Message) -> Unit>
) : Connection {
    // Do not switch order of stream creation, will result in deadlock
    private val outStream = ObjectOutputStream(socket.getOutputStream())
    private val inStream  = ObjectInputStream(socket.getInputStream())
    // Flag to stop handling messages when withHandlers is called
    private var handleMessages = AtomicBoolean(true)
    // Flag to ignore EOF exception when closing socket correctly
    private var shuttingDown = AtomicBoolean(false)

    // TEMPORARY
    override val id = "1"

    init {
        /* TODO: Type check handler map
            for (key in handler.keys)
            if (Message::class in key.supertypes)
         */
        receiveAsync()
    }

    private fun receiveAsync() = GlobalScope.launch {
        while (!socket.isClosed && handleMessages.get()) {
            val msg = try {
                inStream.readObject() as? Message ?: throw Exception("Received invalid object")
            } catch (e: EOFException) {
                if (!shuttingDown.get()) throw e else break
            } catch (e: SocketException) {
                if (!shuttingDown.get()) throw e else break
            }
            val handler = handlers[msg::class] ?: throw Exception("No handler found for message type ${msg::class}")
            handler(this@SocketConnection, msg)
        }
    }

    override fun send(msg: Message) {
        outStream.writeObject(msg)
    }

    override fun close() {
        shuttingDown.set(true)
        socket.close()
    }

    override fun withHandlers(handlers: Map<KClass<*>, (Connection, Message) -> Unit>): Connection {
        // TODO: This should probably kill the coroutine, too, in case it's already blocking on readObject.
        // Note: This should only kill the coroutine if this method is not executed within the coroutine!
        //       Otherwise, it suicides and the new Connection is never created.
        handleMessages.set(false)
        this.handlers = handlers
        return this
    }
}
