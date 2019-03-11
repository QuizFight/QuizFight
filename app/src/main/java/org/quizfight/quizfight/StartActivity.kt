package org.quizfight.quizfight

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View

/**
 * This activity is the first activity of the app
 * It proposes two button to create a game or to join a game
 * @author Aude Nana
 */
class StartActivity : AppCompatActivity() {

    val masterServerIp = "192.168.0.166"
    val gameServerIp = "192.168.0.33"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_start)
    }

    /**
     * This methods shows the AllGamesActivity ,
     * where all available games are showed
     */
    fun showAllGamesActivity(view: View) {
        val intent = Intent(this, AllGamesActivity::class.java)
        intent.putExtra("masterServerIP", masterServerIp)
        intent.putExtra("gameServerIP", gameServerIp)
        startActivity(intent)

    }

    /**
     * This method shows the CreateGameActivity,
     * where the user can create a new Game
     */
    fun showCreateGameActivity(view: View) {
        val intent = Intent(this, CreateGameActivity::class.java)
        intent.putExtra("masterServerIP", masterServerIp)
        intent.putExtra("gameServerIP", gameServerIp)
        startActivity(intent)

    }

}
