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

const val masterServerIP = "10.0.2.2"
const val masterServerPort = 34567

object Client : CoroutineScope, Connection {
    override val coroutineContext = Dispatchers.Main
    private var connection: Connection? = null
    private var lastMessage: Message? = null

    init {
        launch(Dispatchers.IO) {
            val socket = Socket(masterServerIP, masterServerPort)
            connection = SocketConnection(socket, emptyMap())
        }
    }

    val connected: Boolean
        get() = connection != null

    override fun send(msg: Message) {
        lastMessage = msg
        launch(Dispatchers.IO) {connection?.send(msg) }
    }

    override fun withHandlers(handlers: Map<KClass<*>, (Connection, Message) -> Unit>): Connection {
        val extHandlers = handlers + (
            MsgTransferToGameServer::class to { _, msg -> handleServerTransfer(msg as MsgTransferToGameServer) }
        )
        launch(Dispatchers.IO) { connection?.withHandlers(extHandlers) }
        return this
    }

    private fun handleServerTransfer(msg: MsgTransferToGameServer) {
        val oldHandlers = connection?.handlers ?: emptyMap()
        val serverData = msg.gameServer
        val socket = Socket(serverData.ip, serverData.port)
        connection = SocketConnection(socket, oldHandlers)

        // Resend last message that caused transfer
        if (lastMessage != null)
            connection!!.send(lastMessage!!)
    }

    fun reconnectToMaster() = launch(Dispatchers.IO) {
        connection?.close()
        val socket = Socket(masterServerIP, masterServerPort)
        connection = SocketConnection(socket, emptyMap())
    }

    override fun close() { launch(Dispatchers.IO) { connection?.close() } }
    override var handlers: Map<KClass<*>, (Connection, Message) -> Unit>
        get() = connection!!.handlers
        set(value) { connection!!.handlers = value }
    override val id: String
        get() = connection!!.id
}

