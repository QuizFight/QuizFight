package org.quizfight.quizfight

import android.support.v7.app.AppCompatActivity
import org.quizfight.common.SocketConnection
import org.quizfight.common.messages.MsgGameInfo
import org.quizfight.common.messages.MsgQuestion
import org.quizfight.common.question.ChoiceQuestion
import java.net.Socket

class Client(serverIp: String, port: Int , activity: AppCompatActivity) {

    val socket = Socket(serverIp, port)
    val conn : SocketConnection
    val activity = activity

    init {
        conn = SocketConnection(socket,
                mapOf( MsgQuestion ::class to { conn, msg -> receiveQuestion(msg as MsgQuestion )},
                        MsgGameInfo ::class to { conn, msg -> receiveGameInfo(msg as MsgGameInfo )}
                ))

    }

    fun receiveQuestion(msg :MsgQuestion){
        if(activity is QuizActivity) {

            var question: ChoiceQuestion = msg.question as ChoiceQuestion
            activity.showNextQuestion(question)
            Thread.sleep(20000)
            activity.sendScore()
            //MsgRanking anzeigen bevor nextQuestion anzeigen
        }

    }

    fun receiveGameInfo(msg: MsgGameInfo){
        if(activity is CreateGameActivity) {
            activity.showAttemptQuizStartActivity(msg)
        }
    }




}

