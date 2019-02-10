package org.quizfight.quizfight

import org.quizfight.common.Connection
import org.quizfight.common.SocketConnection
import org.quizfight.common.messages.MsgSendAnswer
import org.quizfight.common.messages.MsgSendQuestion
import org.quizfight.common.question.FourAnswersQuestion
import org.quizfight.common.question.Type
import java.net.ServerSocket

class Server(val port: Int) {

    val socket = ServerSocket(port)
    val conn : Connection

    init {
        conn = SocketConnection(socket.accept(), mapOf(
                MsgSendAnswer::class to { conn, msg -> handleAnswer(msg as MsgSendAnswer) }))
    }

    fun handleAnswer(msg: MsgSendAnswer) {
        println("Answer received! The new score is ${msg.score}")

        val question2 = FourAnswersQuestion("Frage 2? ",
                "Test",
                Type.FOUR_ANSWERS_QUESTION,
                "richtig",
                "falsch",
                "falsch",
                "falsch")

        conn.send(MsgSendQuestion(question2))
    }
}

fun main(args: Array<String>) {

    val server = Server(34567)


    val question1 = FourAnswersQuestion("Wie heisst du? ",
            "Test",
            Type.FOUR_ANSWERS_QUESTION,
            "richtig",
            "falsch",
            "falsch",
            "falsch")

    server.conn.send(MsgSendQuestion(question1))
   // server.conn.send(MsgSendQuestion(question2))


    while(true);
}
