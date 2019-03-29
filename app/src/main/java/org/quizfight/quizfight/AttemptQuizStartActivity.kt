package org.quizfight.quizfight

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.View
import kotlinx.android.synthetic.main.activity_attempt_quiz_start.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.quizfight.common.messages.*
import org.quizfight.common.question.ChoiceQuestion
import org.quizfight.common.question.GuessQuestion
import java.util.ArrayList

class AttemptQuizStartActivity :CoroutineScope, AppCompatActivity() {

    private var job = Job()
    override val coroutineContext = Dispatchers.Main + job

    private val context = this

    private var maxPlayers: Int = 0
    private var nickname = ""
    private var questionCountTotal = 0
    private var startGameEnable = false

    private var gameId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attempt_quiz_start)

        //set handlers for the client
        Client.withHandlers(
                mapOf( MsgQuestion ::class to { _, msg -> showQuizActivity(msg as MsgQuestion)},
                        MsgPlayerCount ::class to { _, msg -> updateProgressBar((msg as MsgPlayerCount).playerCount)},
                        MsgGameOver ::class to { _, _ -> restartApplication()}
                ))

        val createdBy = intent.getStringExtra("creator")
        nickname = intent.getStringExtra("nickname")
        val gameName = intent.getStringExtra("gameName")
        maxPlayers = intent.getIntExtra("maxPlayers",0)
        questionCountTotal = intent.getIntExtra("questionCountTotal",0)
        startGameEnable = intent.getBooleanExtra("startEnable", false)
        val playerCount = intent.getIntExtra("playerCount", 0)
        gameId = intent.getStringExtra("gameId")

        //update UI
        updateUI(nickname, createdBy, gameName, questionCountTotal)
        updateProgressBar(playerCount + 1)

        //send request all open games when a user click on button leave
        btn_leave.setOnClickListener {
            sendMsgLeaveGame()
        }
    }


    /**
     * by connection lost, reconnect the client to the master server
     * and restart the application
     */
    fun restartApplication() {
        launch{
            Client.reconnectToMaster()
            val intent = Intent(context, StartActivity::class.java)
            intent.putExtra("restart", true)
            startActivity(intent)
            context.finish()
        }
    }


    /**
     * send message start Game
     * is only enable for the creator of the game.
     * It enable the creator to start a game,
     * without waiting all players to join the game
     */
    fun sendMsgStartGame() {
        Client.send(MsgStartGame())
        btn_start.isEnabled = false
    }

    /**
     * Leave a game
     * ask  the user to confirm the choice first
     */
    fun sendMsgLeaveGame() {

        //ask for confirmation bevor leave
        var ad = AlertDialog.Builder(context)
        ad.setTitle("Warning")
        ad.setMessage("Are you sure you want to leave this game?")

        ad.setPositiveButton("yes") { _, _ ->
            Client.send(MsgLeave())
            context.finish()
            Client.reconnectToMaster()
        }

        ad.setNegativeButton("Cancel") { _, _ ->
            ad.setCancelable(true)
        }
        ad.show()
    }


    /**
     * update the UI
     * if the player is creator of the game,
     * then show the start button
     * else hide it
     */
    fun updateUI(nickname: String, createdBy:String, gameName:String, questionCountTotal: Int){

        tv_nickname.setText(nickname)
        tv_question_count.setText(""+ questionCountTotal)
        tv_game_name.setText(gameName)
        tv_created_by.setText(createdBy)

        if(startGameEnable){
            btn_start.visibility = View.VISIBLE
            btn_start.setOnClickListener {
                sendMsgStartGame()
            }
        } else{
            btn_start.visibility = View.GONE
        }
    }


    /**
     * update the progressbar to show the current count of players
     * that have joined the game
     */
    fun updateProgressBar(players: Int)= launch {

        //update text
        tv_maxplayers.text = ""+ players + "/" + maxPlayers


        //update Bar
        var progress =  players * 100f / maxPlayers
        if(players == maxPlayers)
            progress = 100f
        progressBar.setProgressWithAnimation(progress, 1000)

    }


    /**
     * Switch to the quizactivity and display the first question of the game
     */
    fun showQuizActivity(msg: MsgQuestion) = launch{

        val intent = Intent(context, QuizActivity::class.java)
        intent.putExtra("gameId" , gameId)
        intent.putExtra("nickname", nickname)
        intent.putExtra("questionCountTotal", questionCountTotal)
        intent.putExtra("questionText", msg.question.text)
        intent.putExtra("Category", msg.question.category.name)

        //put 1st question

        //handle ChoiceQuestion
        if(msg.question is ChoiceQuestion) {

            val question = msg.question as ChoiceQuestion

            intent.putExtra("isChoiceQuestion", true)
            intent.putExtra("correctChoice", question.correctChoice)
            var answers = ArrayList<String>()
            answers.add(question.choices[0])
            answers.add(question.choices[1])
            answers.add(question.choices[2])
            answers.add(question.choices[3])

            intent.putStringArrayListExtra("answers", answers)
        }

        //handle GuessQuestion
        else{

            val question = msg.question as GuessQuestion

            intent.putExtra("isChoiceQuestion", false)
            intent.putExtra("correctChoice",question.correctValue)
            intent.putExtra("highest", question.highest)
            intent.putExtra("lowest", question.lowest)
        }

        startActivity(intent)
        saveGameInfo()

        context.finish()
    }


    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }


    override fun onBackPressed() {
        sendMsgLeaveGame()
    }


    /**
     * by connection lost, save game's info on the device
     */
    fun saveGameInfo(){
        val preferences = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putString("gameId", gameId)
        editor.putString("nickname", nickname)
        editor.commit()
    }

}
