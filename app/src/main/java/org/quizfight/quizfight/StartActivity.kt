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

        //bspl fuer client
      /*  val task = object : AsyncTask<Void, Void, Unit>() {
            override fun doInBackground(vararg params: Void?) {
                val client = Client("10.0.2.2", 12345)
              // println("test" + client.question.text)

            }
        }
        task.execute()*/
       // ShowQestionTask(this).execute()

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
