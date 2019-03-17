package org.quizfight.quizfight

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.quizfight.common.Connection
import org.quizfight.common.SocketConnection
import org.quizfight.common.messages.*
import java.lang.Exception
import java.net.Socket
import kotlin.reflect.KClass


object Client : CoroutineScope, Connection {
    override val coroutineContext = Dispatchers.Main
    private var connection: SocketConnection? = null
    private var lastMessage: Message? = null
    private var masterServerIP: String? = null
    private var masterServerPort: Int? = null
    var gameServerIp: String = ""
    var gameServerPort : Int = 0

    val connected: Boolean
        get() = connection != null

    val ip: String?
        get() = connection?.ip

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

        launch(Dispatchers.IO) { connection?.withHandlers(withLogging) }
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
        gameServerIp = serverData.ip
        gameServerPort = serverData.port
    }

    fun reconnectToMaster() = launch(Dispatchers.IO) {

        val handlers = connection?.handlers
        try {
            connection?.close()
        }catch (e : Exception){
            e.printStackTrace()
        }


        Log.d("Connection", "Connection with gameserver closed ")
        Log.d("Connection", "(Re)Connecting to master server $masterServerIP...")
        val socket = Socket(masterServerIP!!, masterServerPort!!)
        connection = SocketConnection(socket, handlers ?: emptyMap())

        Log.d("Connection", "Connected to $masterServerIP")
    }

    fun reconnectToGameServer(ip: String, port: Int) = launch(Dispatchers.IO) {
        Log.d("Connection", "Reconnecting to GameServer $ip and port $port" )

        val handlers = connection?.handlers
        connection?.close()

        val socket = Socket(ip, port)
        connection = SocketConnection(socket, handlers ?: emptyMap())
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
