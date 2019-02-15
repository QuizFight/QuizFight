package org.quizfight.quizfight

import android.content.Context
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_start.*
import org.quizfight.common.SocketConnection
import org.quizfight.common.messages.MsgSendQuestion
import org.quizfight.common.question.FourAnswersQuestion
import java.net.Socket

class Client(serverIp: String, port: Int , activity: QuizActivity?) {

    val socket = Socket(serverIp, port)
    val conn : SocketConnection
    lateinit var question : FourAnswersQuestion
    val ac = activity

    init {
        conn = SocketConnection(socket,
                mapOf( MsgSendQuestion ::class to { conn, msg -> receiveQuestion(msg as MsgSendQuestion )}))

    }

    fun receiveQuestion(msg :MsgSendQuestion){
        question = msg.question as FourAnswersQuestion
        ac?.showNextQuestion(question)
        Thread.sleep(20000)
        ac?.sendScore()

    }





}

