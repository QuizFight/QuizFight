package org.quizfight.quizfight

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_start.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.quizfight.common.MASTER_PORT
import org.quizfight.common.messages.*
import org.quizfight.common.question.ChoiceQuestion
import org.quizfight.common.question.GuessQuestion
import java.util.ArrayList




/**
 * This activity is the first activity of the app
 * It proposes two button to create a game or to join a game
 * @author Aude Nana
 */
class StartActivity : CoroutineScope, AppCompatActivity() {

    private var job = Job()
    override val coroutineContext = Dispatchers.Main + job

    private var masterServerIp = "10.9.40.89"

    private var gameId = ""
    private var nickname = ""
    private var gameServerIp = ""
    private var gameServerPort = 0
    private var context = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        Client.initialize(masterServerIp, MASTER_PORT)
        Client.onGameServerLeft += { clearGameInfo() }
        Client.onGameServerJoined += { ip, port -> saveGameServerInfo(ip, port) }

        //check if app is started after attemptQuiz or not
        //if so , show toast else 2 possibilities
        //even the app was stopped during a quiz
        //then read games info and send ReJoin to gameserver
        //or the app is started for the first time
        //then just create a client
        val restart = intent.getBooleanExtra("restart", false)
        if (restart) {
            launch {
                Toast.makeText(context, "Sorry this game is no more available", Toast.LENGTH_LONG).show()
            }
        } else {
            // if user was already in a game try rejoining
            if (readGamesInfo()) {
                Log.d("Connection", "Found aborted game, reconnecting")

                Client.reconnectToGameServer(gameServerIp, gameServerPort, mapOf(
                    MsgQuestion::class to { _, msg -> showQuizActivity(msg as MsgQuestion) },
                    MsgRanking::class to { _, _ -> /* Ignore rankings - wait for next question */ },
                    MsgGameOver::class to { conn, _ -> conn.close(); Client.reconnectToMaster() },
                    MsgPlayerCount::class to { _, _ -> /* Not relevant for rejoin */ }
                ))
                Client.send(MsgRejoin(gameId, nickname))
            } else {
                Log.d("Connection", "connect to master ")
                Client.reconnectToMaster()
            }
        }
    }

    /**
     * This methods shows the AllGamesActivity ,
     * where all open games are displayed
     */
    fun showAllGamesActivity(view: View) {
        val intent = Intent(this, AllGamesActivity::class.java)
        if (!ed_masterServerIp.text.toString().isEmpty()) {
            masterServerIp = ed_masterServerIp.text.toString()
        }
        startActivity(intent)

    }

    /**
     * This method shows the CreateGameActivity,
     * where the user can create a new Game
     */
    fun showCreateGameActivity(view: View) {
        val intent = Intent(this, CreateGameActivity::class.java)
        startActivity(intent)
    }

    /**
     * Check if the app starts after a connection failure during a game or not
     * and read game's info
     */
    fun readGamesInfo(): Boolean {
        val preferences = this.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)

        gameId = preferences.getString("gameId", "")
        nickname = preferences.getString("nickname", "")
        gameServerIp = preferences.getString("gameServerIp", "")
        gameServerPort = preferences.getInt("gameServerPort", 1)
        if (!gameId.isEmpty()) {
            return true
        }
        return false
    }

    private fun clearGameInfo() {
        Log.d("Connection", "Clearing old connection info")
        val preferences = this.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.clear()
        editor.commit()
    }

    private fun saveGameServerInfo(ip: String, port: Int) {
        Log.d("Connection", "Saving connection info")
        val preferences = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putString("gameServerIp", ip)
        editor.putInt("gameServerPort", port)
        editor.commit()
    }


    /**
     * Show the next question, if the player could rejoin a game
     */
    fun showQuizActivity(msg: MsgQuestion) = launch {

        val intent = Intent(context, QuizActivity::class.java)
        intent.putExtra("gameId", gameId)
        intent.putExtra("nickname", nickname)
        intent.putExtra("questionCountTotal", 2)   //to modify
        intent.putExtra("questionText", msg.question.text)
        intent.putExtra("Category", msg.question.category.name)

        //put 1st question

        //handle ChoiceQuestion
        if (msg.question is ChoiceQuestion) {

            val question = msg.question as ChoiceQuestion

            intent.putExtra("isChoiceQuestion", true)
            intent.putExtra("correctChoice", question.correctChoice)
            var answers = ArrayList<String>()
            answers.add(question.choices[0])
            answers.add(question.choices[1])
            answers.add(question.choices[2])

            intent.putStringArrayListExtra("answers", answers)
        }

        //handle GuessQuestion
        else {

            val question = msg.question as GuessQuestion

            intent.putExtra("isChoiceQuestion", false)
            intent.putExtra("correctChoice", question.correctValue)
            intent.putExtra("highest", question.highest)
            intent.putExtra("lowest", question.lowest)
        }

        startActivity(intent)
        context.finish()
    }

    override fun onBackPressed() {
        var ad = AlertDialog.Builder(context)
        ad.setTitle("Warning")
        ad.setMessage("Are you sure you want to leave the application ?")

        ad.setPositiveButton("yes") { _, _ ->
            Client.close()
            context.finish()
            job.cancel()
        }

        ad.setNegativeButton("Cancel") { _, _ ->
            ad.setCancelable(true)
        }

        ad.show()
    }



}
