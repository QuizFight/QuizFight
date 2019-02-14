package org.quizfight.server

import org.quizfight.common.Connection
import org.quizfight.common.SocketConnection
import org.quizfight.common.messages.*
import org.quizfight.common.question.FourAnswersQuestion
import java.net.Socket

class Client(private val serverIp: String, private val port: Int) {

    private var socket = Socket(serverIp, port)
    var connection : SocketConnection
    lateinit var question : FourAnswersQuestion
    lateinit var games : List<GameData>

    init {
        connection = SocketConnection(socket,
                mapOf(  MsgGameList::class to { conn, msg -> acceptGameList(conn, msg as MsgGameList) },
                        MsgTransferToGameServer::class to { conn, msg -> acceptGameServer(conn, msg as MsgTransferToGameServer)
                        }
                ))
        connection.send(MsgRequestAllGames())
    }

    private fun receiveQuestion(conn : Connection, msg : MsgSendQuestion){
        question = msg.question as FourAnswersQuestion
        println("Client received question. It is: ${question.text}")

    }

    private fun acceptGameList(conn : Connection, msg : MsgGameList) {

        println("Client received MsgGameList from Master")
        games = msg.gameList
    }

    private fun acceptGameServer(conn : Connection, msg : MsgTransferToGameServer) {
        println("Client received MsgTransferToGameServer. IP is: ${msg.gameServer.ip}")

        conn.close()
        socket = Socket(msg.gameServer.ip, msg.gameServer.port)
        connection = SocketConnection(socket, mapOf(
                MsgSendQuestion ::class to { conn, msg -> receiveQuestion(conn, msg as MsgSendQuestion )}))
    }

}
