package org.quizfight.quizfight

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class GameDetailActivity : AppCompatActivity() {

    var questionCountTotal : Int = 0;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_detail)

        //muss vom Server gelesen werden
        questionCountTotal = 5
    }

    fun showQuizActivity(view: View) {
        // Create an Intent to start the AllGamesActivity
        val intent = Intent(this, QuizActivity::class.java)
        intent.putExtra("questionCountTotal" , questionCountTotal)
        // Start the new activity.
        startActivity(intent)

    }
}
