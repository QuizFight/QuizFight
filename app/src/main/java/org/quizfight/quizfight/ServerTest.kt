package org.quizfight.quizfight

import org.quizfight.common.Connection
import org.quizfight.common.SocketConnection
import org.quizfight.common.messages.MsgSendQuestion
import org.quizfight.common.question.FourAnswersQuestion
import org.quizfight.common.question.Types
import java.net.ServerSocket

class Server(val port: Int) {

    val socket = ServerSocket(port)
    val conn : Connection

    init {
        conn = SocketConnection(socket.accept(), mapOf(
                MsgSendQuestion::class to { conn, msg ->  }))
    }

}

fun main(args: Array<String>) {

    val server = Server(12345)

    val fourAnswersQuestion = FourAnswersQuestion("Wie heisst du? ",
            "Test",
            Types.FOUR_ANSWERS.id,
            "richtig",
            "falsch",
            "falsch",
            "falsch")

    server.conn.send(MsgSendQuestion(fourAnswersQuestion))

    while(true);
}
