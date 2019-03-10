package org.quizfight.quizfight

import android.support.v7.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.quizfight.common.Connection
import org.quizfight.common.SocketConnection
import org.quizfight.common.messages.*
import org.quizfight.common.question.ChoiceQuestion
import java.net.Socket
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass

const val masterServerIP = "10.0.0.2"
const val masterServerPort = 34567

object Client : CoroutineScope, Connection {
    override val coroutineContext = Dispatchers.Main
    private var connection: Connection? = null

    init {
        launch(Dispatchers.IO) {
            val socket = Socket(masterServerIP, masterServerPort)
            connection = SocketConnection(socket, emptyMap())
        }
    }

    val connected: Boolean
        get() = connection != null

    override fun send(msg: Message) { launch(Dispatchers.IO) { connection?.send(msg) } }
    override fun close() { launch(Dispatchers.IO) { connection?.close() } }
    override fun withHandlers(handlers: Map<KClass<*>, (Connection, Message) -> Unit>): Connection {
        launch(Dispatchers.IO) { connection?.withHandlers(handlers) }
        return this
    }
    override var handlers = connection!!.handlers
    override val id = connection!!.id
}

