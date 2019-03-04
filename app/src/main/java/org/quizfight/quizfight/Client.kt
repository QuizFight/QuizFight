package org.quizfight.quizfight

import org.quizfight.common.SocketConnection
import org.quizfight.common.messages.MsgQuestion
import org.quizfight.common.question.ChoiceQuestion
import java.net.Socket

class Client(serverIp: String, port: Int , activity: QuizActivity?) {

    val socket = Socket(serverIp, port)
    val conn : SocketConnection
    lateinit var question : ChoiceQuestion
    val ac = activity

    init {
        conn = SocketConnection(socket,
                mapOf( MsgQuestion ::class to { conn, msg -> receiveQuestion(msg as MsgQuestion )}))

    }

    fun receiveQuestion(msg :MsgQuestion){
        question = msg.question as ChoiceQuestion
        ac?.showNextQuestion(question)
        Thread.sleep(20000)
        ac?.sendScore()

    }





}

