package org.quizfight.quizfight

import org.quizfight.common.Connection
import org.quizfight.common.SocketConnection
import org.quizfight.common.messages.MsgSendAnswer
import org.quizfight.common.messages.MsgSendQuestion
import org.quizfight.common.messages.MsgStartGame
import org.quizfight.common.question.FourAnswersQuestion
import org.quizfight.common.question.Question
import org.quizfight.common.question.Type
import java.net.ServerSocket

class Server(val port: Int) {

    val socket = ServerSocket(port)
    val conn : Connection
    val questionList = arrayListOf<Question> ()
    var count : Int = 0

    init {
        initQuestionList()
        conn = SocketConnection(socket.accept(), mapOf(
                MsgSendAnswer::class to { conn, msg -> handleAnswer(msg as MsgSendAnswer) }))
               // ,MsgStartGame::class to
    }

    fun handleAnswer(msg: MsgSendAnswer) {
        println("Answer received! The new score is ${msg.score}")
        if(count < questionList.size) {
            conn.send(MsgSendQuestion(questionList[count]))
            count++
        }
    }

    fun initQuestionList() {
        val q1 = FourAnswersQuestion("Welcher dieser Herren ist Teil des Videospielunternehmens Nintendo? ",
                "Test",
                Type.FOUR_ANSWERS_QUESTION,
                "Shigeru Miyamoto",
                "Hillenburg",
                "Turing",
                "Schmitz")

        val q2 = FourAnswersQuestion("In welchem Jahr erschien in Deutschland das Super Nintendo Entertainment System? ",
                "Test",
                Type.FOUR_ANSWERS_QUESTION,
                "1992",
                "1990",
                "1991",
                "1993")

        val q3 = FourAnswersQuestion("welchem Land steht der schiefe Turm von Pisa?",
                "Test",
                Type.FOUR_ANSWERS_QUESTION,
                "Italien",
                "Turmland",
                "Portugal",
                "Bulgarien")

        val q4 = FourAnswersQuestion("Wie lautet die Hauptstadt von Weissrussland?",
                "Test",
                Type.FOUR_ANSWERS_QUESTION,
                "Minsk",
                "Nimsk",
                "Tallinn",
                "Riga")

        val q5 = FourAnswersQuestion("Wann wurde offiziell der EURO eingefuehrt?",
                "Test",
                Type.FOUR_ANSWERS_QUESTION,
                "1. Januar 2002",
                "12. Januar 2002",
                "1. Januar 2001",
                "12. Januar 2001")

        questionList.addAll(listOf(q1, q2,q3,q4,q5))
    }
}



fun main(args: Array<String>) {

    val server = Server(34567)


    val question1 = FourAnswersQuestion("Wann wurde offiziell der EURO eingefuehrt?",
            "Test",
            Type.FOUR_ANSWERS_QUESTION,
            "1. Januar 2002",
            "12. Januar 2002",
            "1. Januar 2001",
            "12. Januar 2001")

    server.conn.send(MsgSendQuestion(question1))


    while(true);
}
