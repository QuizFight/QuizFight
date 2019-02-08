package org.quizfight.quizfight

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class GameDetailActivity : AppCompatActivity() {

    var questionCountTotal : Int = 5
    var gameId : Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_detail)

        updateUi()
    }

    fun showQuizActivity(view: View) {
        // Create an Intent to start the AllGamesActivity
        val intent = Intent(this, QuizActivity::class.java)
        intent.putExtra("questionCountTotal" , questionCountTotal)
        intent.putExtra("gameId" , gameId)
        // Start the new activity.
        startActivity(intent)

    }

    fun updateUi(){

        questionCountTotal = intent.getIntExtra("questionCountTotal",0 )
        gameId = intent.getIntExtra("gameId",0 )
        val createBy = intent.getStringExtra("gameName")
        val maxPlayer = intent.getIntExtra("maxPlayer", 0 )


        if(intent.hasExtra("nickname")) {
            val nickname = intent.getStringExtra("nickname")
        }

        //verknupfung mit View hier unten
    }

}
