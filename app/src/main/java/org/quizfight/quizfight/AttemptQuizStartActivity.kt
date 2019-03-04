package org.quizfight.quizfight

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_attempt_quiz_start.*

class AttemptQuizStartActivity : AppCompatActivity() {

    var gameId : Int = 0;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attempt_quiz_start)

        gameId = intent.getIntExtra("gameId",0)
        var createdBy = intent.getStringExtra("createdBy")
        var nickname = intent.getStringExtra("nickname")
        var gameName = intent.getStringExtra("gameName")
        var maxPlayers = intent.getIntExtra("maxPlayers",0)
        var questionCountTotal = intent.getIntExtra("questionCountTotal",0)

        updateUI(nickname, createdBy, gameName, maxPlayers, questionCountTotal)

    }

    fun updateUI(nickname: String, createdBy:String, gameName:String, maxPlayers: Int,  questionCountTotal: Int){

        tv_nickname.setText(nickname)
        tv_question_count.setText(""+ questionCountTotal)
        tv_game_name.setText(gameName)
        tv_created_by.setText(createdBy)

    }
}
