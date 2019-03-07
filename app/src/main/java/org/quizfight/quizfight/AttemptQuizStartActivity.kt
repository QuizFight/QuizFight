package org.quizfight.quizfight

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_attempt_quiz_start.*



class AttemptQuizStartActivity : AppCompatActivity() {

    private var gameId : Int = 0;
    private var maxPlayers: Int = 0;
    private var nickname : String = ""
    private var players = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attempt_quiz_start)

        gameId = intent.getIntExtra("gameId",0)
        var createdBy = intent.getStringExtra("createdBy")
        nickname = intent.getStringExtra("nickname")
        var gameName = intent.getStringExtra("gameName")
        maxPlayers = intent.getIntExtra("maxPlayers",0)
        var questionCountTotal = intent.getIntExtra("questionCountTotal",0)

        updateUI(nickname, createdBy, gameName, questionCountTotal)
        updateProgressBar()

    }

    fun updateUI(nickname: String, createdBy:String, gameName:String, questionCountTotal: Int){

        tv_nickname.setText(nickname)
        tv_question_count.setText(""+ questionCountTotal)
        tv_game_name.setText(gameName)
        tv_created_by.setText(createdBy)

    }

    fun updateProgressBar(){

        //update text
        tv_maxplayers.text = ""+ players + "/" + maxPlayers

        //update Bar
        players++
        var progress =  players * 100f / maxPlayers
        if(players == maxPlayers)
            progress = 100f
        progressBar.setProgressWithAnimation(progress, 1000)

    }
}
