package org.quizfight.quizfight

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.quizfight.common.Connection
import org.quizfight.common.SocketConnection
import org.quizfight.common.messages.*
import java.net.Socket
import kotlin.reflect.KClass


object Client : CoroutineScope, Connection {
    override val coroutineContext = Dispatchers.Main
    private var connection: SocketConnection? = null
    private var lastMessage: Message? = null
    private var masterServerIP: String? = null
    private var masterServerPort: Int? = null

    val connected: Boolean
        get() = connection != null

    val ip: String?
        get() = connection?.ip

    override fun send(msg: Message) {
        lastMessage = msg
        launch(Dispatchers.IO) {connection?.send(msg) }
        Log.d("Connection", "Sent message $msg to ${connection?.ip}")
    }

    override fun withHandlers(handlers: Map<KClass<*>, (Connection, Message) -> Unit>): Connection {
        val extHandlers = handlers + (
                MsgTransferToGameServer::class to { _, msg -> handleServerTransfer(msg as MsgTransferToGameServer) }
                )
        launch(Dispatchers.IO) { connection?.withHandlers(extHandlers) }
        return this
    }

    private fun handleServerTransfer(msg: MsgTransferToGameServer) {
        connection?.close()
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
        val socket = Socket(masterServerIP!!, masterServerPort!!)
        connection = SocketConnection(socket, emptyMap())
    }

    fun setMasterServer(ip: String, port: Int) {
        masterServerIP = ip
        masterServerPort = port

        reconnectToMaster()
    }

    override fun close() { launch(Dispatchers.IO) { connection?.close() } }
    override var handlers: Map<KClass<*>, (Connection, Message) -> Unit>
        get() = connection!!.handlers
        set(value) { connection!!.handlers = value }
    override val id: String
        get() = connection!!.id
}
