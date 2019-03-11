package org.quizfight.quizfight

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_start.*

/**
 * This activity is the first activity of the app
 * It proposes two button to create a game or to join a game
 * @author Aude Nana
 */
class StartActivity : AppCompatActivity() {

    var masterServerIp = "192.168.0.166"

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
        //if(!ed_masterServerIp.text.isEmpty())
        //    masterServerIp = ed_masterServerIp.text.toString()
        intent.putExtra("masterServerIP", masterServerIp)
        startActivity(intent)

    }

    /**
     * This method shows the CreateGameActivity,
     * where the user can create a new Game
     */
    fun showCreateGameActivity(view: View) {
        val intent = Intent(this, CreateGameActivity::class.java)
        intent.putExtra("masterServerIP", masterServerIp)
        startActivity(intent)

    }

}
