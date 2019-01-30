package org.quizfight.quizfight

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import kotlinx.android.synthetic.main.activity_all_games.*

class AllGamesActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_games)

        // read a list of games from server
        val gameList = listOf("Game1", "Game2", "Game3", "Game4", "Game5", "Game6", "Game7")

        // Create an ArrayAdapter from List
        val adapter = ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                gameList
        )

        // Finally, data bind the list view object with adapter
        all_games_container.adapter = adapter;

        // Set an item click listener for ListView
        all_games_container.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            // Get the selected item text from ListView
            val selectedGame= parent.getItemAtPosition(position) as String
            val intent = Intent(this, GameDetailActivity::class.java)
            intent.putExtra("game", selectedGame)
            startActivity(intent)

        }


    }

}