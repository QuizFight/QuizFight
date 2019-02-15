package org.quizfight.quizfight

import android.content.Intent
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast

/**
 * This activity is the first activity of the app
 * It proposes two button to create a game or to join a game
 * @author Aude Nana
 */
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
        startActivity(intent)

    }

    /**
     * This method shows the CreateGameActivity,
     * where the user can create a new Game
     */
    fun showCreateGameActivity(view: View) {
       /* val myToast = Toast.makeText(this, "CreateGameActivity comes soon", Toast.LENGTH_SHORT)
        myToast.show()*/
        val intent = Intent(this, CreateGameActivity::class.java)
        startActivity(intent)
    }

}
