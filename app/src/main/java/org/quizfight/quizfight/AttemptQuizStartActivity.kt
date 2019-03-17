package org.quizfight.quizfight

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
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
    private var nickname : String = ""
    private var questionCountTotal = 0
    private var startGameEnable : Boolean = false

    private var gameId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attempt_quiz_start)

        Client.withHandlers(
                mapOf( MsgQuestion ::class to { conn, msg -> showQuizActivity(msg as MsgQuestion)},
                        MsgPlayerCount ::class to { conn, msg -> updateProgressBar((msg as MsgPlayerCount).playerCount)}))

        var createdBy = intent.getStringExtra("createdBy")
        nickname = intent.getStringExtra("nickname")
        var gameName = intent.getStringExtra("gameName")
        maxPlayers = intent.getIntExtra("maxPlayers",0)
        questionCountTotal = intent.getIntExtra("questionCountTotal",0)
        startGameEnable = intent.getBooleanExtra("startEnable", false)
        val playerCount = intent.getIntExtra("playerCount", 0)

        gameId = intent.getStringExtra("gameId")

        updateUI(nickname, createdBy, gameName, questionCountTotal)
        updateProgressBar(playerCount + 1)

        btn_leave.setOnClickListener {
            sendMsgLeaveGame()
        }
    }


    fun sendMsgStartGame() {
        Client.send(MsgStartGame())
        btn_start.isEnabled = false
    }

    fun sendMsgLeaveGame() {
        Client.send(MsgLeave())
        launch { context.finish() }
        Client.reconnectToMaster()
    }


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

    fun updateProgressBar(players: Int)= launch {

        //update text
        tv_maxplayers.text = ""+ players + "/" + maxPlayers

        //update Bar
        var progress =  players * 100f / maxPlayers
        if(players == maxPlayers)
            progress = 100f
        progressBar.setProgressWithAnimation(progress, 1000)

    }


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
        saveGameServersInfo()

        context.finish()
    }


    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    override fun onBackPressed() {
        sendMsgLeaveGame()
    }


    fun saveGameServersInfo(){
        val preferences = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putString("gameServerIp", Client.gameServerIp)
        editor.putInt("gameServerPort", Client.gameServerPort)
        editor.putString("gameId", gameId)
        editor.putString("nickname", nickname)
        editor.commit()

    }

}
