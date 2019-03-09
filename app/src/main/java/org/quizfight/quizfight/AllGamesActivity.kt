package org.quizfight.quizfight

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import kotlinx.android.synthetic.main.activity_all_games.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.quizfight.common.messages.GameData
import org.quizfight.common.messages.MsgRequestOpenGames

/**
 * This activity allows the user to join a game
 * The user should select one game from the list to join it
 * @author Aude Nana
 */
class AllGamesActivity : CoroutineScope, AppCompatActivity() {

    private var job = Job()
    override val coroutineContext = Dispatchers.Main + job

    private val context = this
    private lateinit var allOpenGames : List<GameData>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_games)

        sendRequestOpenGame()

        // Set an item click listener for ListView
        all_games_container.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            // Get the selected item text from ListView
           // val selected= parent.getItemAtPosition(position) as String

            val selectedGame = allOpenGames[position]

            val intent = Intent(this, GameDetailActivity::class.java)
            intent.putExtra("gameName", selectedGame.name)
            intent.putExtra("gameId", selectedGame.id)
            intent.putExtra("questionCountTotal", selectedGame.questionCount)
            intent.putExtra("maxPlayers", selectedGame.maxPlayers)

            startActivity(intent)
        }

        btn_sync.setOnClickListener {
            sendRequestOpenGame()
        }

    }


    fun sendRequestOpenGame(){
        launch(Dispatchers.IO) {
            val client = Client("10.0.2.2", 23456, this@AllGamesActivity)
            client.conn.send(MsgRequestOpenGames())
        }

    }


    /**
     * Displays all available games
     */
    fun showGames(gameList: List<GameData>) = launch {

        if(gameList.isEmpty()){
            all_games_container.visibility = View.INVISIBLE
        } else {
            all_games_container.visibility = View.VISIBLE
        }

        allOpenGames = gameList
        var gameNameList = ArrayList<String>()

        for ( game in gameList){
            gameNameList.add(game.name)
        }

        val adapter = ArrayAdapter<String>(
                context,
                android.R.layout.simple_list_item_1,
                gameNameList
        )
        all_games_container.adapter = adapter;
    }


    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

}