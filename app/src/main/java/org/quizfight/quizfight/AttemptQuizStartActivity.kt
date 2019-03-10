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
import org.quizfight.common.messages.MsgStartGame

class AttemptQuizStartActivity :CoroutineScope, AppCompatActivity() {

    private var gameId : String = "";
    private var maxPlayers: Int = 0;
    private var nickname : String = ""
    private var players = 0
    private var questionCountTotal = 0

    private var startGameEnable : Boolean = false

    private var job = Job()
    override val coroutineContext = Dispatchers.Main + job

    private val context = this

    private lateinit var client : Client

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attempt_quiz_start)

        gameId = intent.getStringExtra("gameId")
        var createdBy = intent.getStringExtra("createdBy")
        nickname = intent.getStringExtra("nickname")
        var gameName = intent.getStringExtra("gameName")
        maxPlayers = intent.getIntExtra("maxPlayers",0)
        questionCountTotal = intent.getIntExtra("questionCountTotal",0)
        startGameEnable = intent.getBooleanExtra("startEnable", false)

        updateUI(nickname, createdBy, gameName, questionCountTotal)
        updateProgressBar()

    }

    fun sendMsgStartGame() = launch(Dispatchers.IO){
        client = Client("192.168.0.32", 23456, this@AttemptQuizStartActivity)
        client.conn.send(MsgStartGame())

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
                showQuizActivity()
            }
        } else{
            btn_start.visibility = View.GONE
        }

    }

    fun updateProgressBar() {

        players++
        //update text
        tv_maxplayers.text = ""+ players + "/" + maxPlayers

        //update Bar
        var progress =  players * 100f / maxPlayers
        if(players == maxPlayers)
            progress = 100f
        progressBar.setProgressWithAnimation(progress, 1000)

    }


    fun showQuizActivity() = launch {
        // Create an Intent to start the AllGamesActivity
        val intent = Intent(context, QuizActivity::class.java)
        intent.putExtra("gameId" , gameId)
        intent.putExtra("questionCountTotal" , questionCountTotal)
        startActivity(intent)

    }


    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
