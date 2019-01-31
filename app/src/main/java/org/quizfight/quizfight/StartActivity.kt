package org.quizfight.quizfight

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_all_games.*

class StartActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)
    }

    /**
     * This methods shows the AllGamesActivity ,
     * where all available games are showed
     */
    fun showAllGamesActivity(view: View) {
        // Create an Intent to start the AllGamesActivity
        val intent = Intent(this, AllGamesActivity::class.java)
        // Start the new activity.
        startActivity(intent)

    }

    /**
     * This method shows the CreateGameActivity,
     * where the user can create a new Game
     */
    fun showCreateGameActivity(view: View) {
        // val myToast = Toast.makeText(this, message, duration);
        val myToast = Toast.makeText(this, "CreateGameActivity comes soon", Toast.LENGTH_SHORT)
        myToast.show()
    }
}
