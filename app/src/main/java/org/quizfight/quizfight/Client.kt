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

    var onGameServerJoined: List<(String, Int) -> Unit> = emptyList()
    var onGameServerLeft: List<() -> Unit> = emptyList()

    val connected: Boolean
        get() = connection != null

    val ip: String?
        get() = connection?.ip

    fun initialize(masterServerIP: String, masterServerPort: Int) {
        this.masterServerIP = masterServerIP
        this.masterServerPort = masterServerPort
    }

    override fun send(msg: Message) {
        while (!connected);

        lastMessage = msg
        launch(Dispatchers.IO) {connection?.send(msg) }
        Log.d("Connection", "Sent message $msg to ${connection?.ip}:${connection?.socket?.port}")
    }

    override fun withHandlers(handlers: Map<KClass<*>, (Connection, Message) -> Unit>): Connection {
        val extHandlers = handlers + (
            MsgTransferToGameServer::class to { _, msg -> handleServerTransfer(msg as MsgTransferToGameServer) }
        )

        val withLogging = extHandlers.mapValues {
            { conn: Connection, msg: Message ->
                Log.d("Connection",  "Received message $msg from ${connection?.ip}")
                it.value(conn, msg)
            }
        }

        launch(Dispatchers.IO) {
            Log.d("Connection", "Updated handlers")
            connection?.withHandlers(withLogging)
        }
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
            send(lastMessage!!)

        onGameServerJoined.forEach { it(serverData.ip, serverData.port) }
    }

    fun reconnectToMaster() = launch(Dispatchers.IO) {
        val handlers = connection?.handlers

        connection?.close()
        if (connection != null) {
            Log.d("Connection", "Connection with gameserver closed ")
            onGameServerLeft.forEach { it() }
        }

        Log.d("Connection", "(Re)Connecting to master server $masterServerIP...")
        val socket = Socket(masterServerIP!!, masterServerPort!!)
        connection = SocketConnection(socket, handlers ?: emptyMap())

        Log.d("Connection", "Connected to $masterServerIP")
    }

    fun reconnectToGameServer(ip: String, port: Int, handlers: Map<KClass<*>, (Connection, Message) -> Unit>) = launch(Dispatchers.IO) {
        Log.d("Connection", "Reconnecting to GameServer $ip and port $port" )
        connection?.close()
        connection = null

        val socket = Socket(ip, port)
        connection = SocketConnection(socket, handlers)
    }

    override fun close() { launch(Dispatchers.IO) { connection?.close() } }
    override var handlers: Map<KClass<*>, (Connection, Message) -> Unit>
        get() = connection!!.handlers
        set(value) { connection!!.handlers = value }
    override val id: String
        get() = connection!!.id
}
