package org.quizfight.quizfight

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_attempt_quiz_start.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.quizfight.common.SocketConnection
import org.quizfight.common.messages.*
import org.quizfight.common.question.ChoiceQuestion
import java.net.Socket

class AttemptQuizStartActivity :CoroutineScope, AppCompatActivity() {

    private var gameId : String = ""
    private var maxPlayers: Int = 0
    private var nickname : String = ""
    private var questionCountTotal = 0

    private var startGameEnable : Boolean = false

    private lateinit var conn : SocketConnection
    private var job = Job()
    override val coroutineContext = Dispatchers.Main + job

    private val context = this

    var masterServerIp = ""
    var gameServerIp = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attempt_quiz_start)


        masterServerIp = intent.getStringExtra("masterServerIP")
        gameServerIp = intent.getStringExtra("gameServerIP")

        launch(Dispatchers.IO) {
            Client.connection?.withHandlers(
                    mapOf( MsgQuestion ::class to { conn, msg -> showQuizActivity((msg as MsgQuestion).question as ChoiceQuestion)},
                            MsgPlayerCount ::class to { conn, msg -> updateProgressBar((msg as MsgPlayerCount).playerCount)}))
        }

        gameId = intent.getStringExtra("gameId")
        var createdBy = intent.getStringExtra("createdBy")
        nickname = intent.getStringExtra("nickname")
        var gameName = intent.getStringExtra("gameName")
        maxPlayers = intent.getIntExtra("maxPlayers",0)
        questionCountTotal = intent.getIntExtra("questionCountTotal",0)
        startGameEnable = intent.getBooleanExtra("startEnable", false)
        val playerCount = intent.getIntExtra("playerCount", 1)

        updateUI(nickname, createdBy, gameName, questionCountTotal)
        updateProgressBar(playerCount + 1)

        btn_leave.setOnClickListener {
            sendMsgLeaveGame()
        }

    }


    fun sendMsgStartGame() {
        launch(Dispatchers.IO) {
            conn = SocketConnection(Socket(gameServerIp, 45678), mapOf())
            conn.send(MsgStartGame())
        }
        launch { context.finish() }

    }

    fun sendMsgLeaveGame() {
        launch(Dispatchers.IO) {
            conn = SocketConnection(Socket(gameServerIp, 45678), mapOf())
            conn.send(MsgLeave())
        }
        launch { context.finish() }
    }


    fun updateUI(nickname: String, createdBy:String, gameName:String, questionCountTotal: Int){

        tv_nickname.setText(nickname)
        tv_question_count.setText(""+ questionCountTotal)
        tv_game_name.setText(gameName)
        tv_created_by.setText(createdBy)



        if(startGameEnable){
            btn_start.visibility = View.VISIBLE
            btn_start.setOnClickListener {
                sendMsgStartGame()
            }
        } else{
            btn_start.visibility = View.GONE
        }

    }

    fun updateProgressBar(players: Int)= launch {

        //update text
        tv_maxplayers.text = ""+ players + "/" + maxPlayers

        //update Bar
        var progress =  players * 100f / maxPlayers
        if(players == maxPlayers)
            progress = 100f
        progressBar.setProgressWithAnimation(progress, 1000)

    }


    fun showQuizActivity(question: ChoiceQuestion) = launch{
        // Create an Intent to start the AllGamesActivity
        val intent = Intent(context, QuizActivity::class.java)
        intent.putExtra("gameId" , gameId)
        intent.putExtra("questionCountTotal" , questionCountTotal)
        intent.putExtra("masterServerIP", masterServerIp)
        intent.putExtra("gameServerIP", gameServerIp)

        //put 1. question
        intent.putExtra("questionText" , question.text)
        intent.putExtra("correctChoice" , question.correctChoice)
      /*  intent.putExtra("masterServerIP", masterServerIp)
        intent.putExtra("gameServerIP", gameServerIp)*/

        startActivity(intent)
        context.finish()
    }


    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
