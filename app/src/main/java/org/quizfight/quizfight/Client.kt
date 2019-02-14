package org.quizfight.quizfight

import org.quizfight.common.Connection
import org.quizfight.common.SocketConnection
import org.quizfight.common.messages.*
import org.quizfight.common.question.FourAnswersQuestion
import java.net.Socket

class Client(val serverIp: String, val port: Int , val activity: QuizActivity?) {

    private var socket = Socket(serverIp, port)
    private var connection : SocketConnection
    lateinit var question : FourAnswersQuestion
    private lateinit var games : List<GameData>

    init {
        connection = SocketConnection(socket,
                mapOf(  MsgGameList::class to { conn, msg -> acceptGameList(conn, msg as MsgGameList) },
                        MsgTransferToGameServer::class to { conn, msg -> acceptGameServer(conn, msg as MsgTransferToGameServer) }
                ))
        connection.send(MsgRequestAllGames())
    }

    fun receiveQuestion(msg :MsgSendQuestion){
        question = msg.question as FourAnswersQuestion
        //activity?.showNextQuestion(question)

    }

    private fun acceptGameList(conn : Connection, msg : MsgGameList) {

        println("Client received MsgGameList from Master")
        msg.gameList.forEach() {game -> println(game.gameName)}
        games = msg.gameList
    }

    private fun acceptGameServer(conn : Connection, msg : MsgTransferToGameServer) {
        println("Client received MsgTransferToGameServer. IP is: ${msg.gameServer.ip}")

        conn.close()
        socket = Socket(msg.gameServer.ip, msg.gameServer.port)
        connection = SocketConnection(socket, mapOf(MsgSendQuestion ::class to { conn, msg -> receiveQuestion(msg as MsgSendQuestion )}))
    }

}


