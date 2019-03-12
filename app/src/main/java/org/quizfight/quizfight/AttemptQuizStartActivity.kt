package org.quizfight.quizfight

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
import java.util.ArrayList

class AttemptQuizStartActivity :CoroutineScope, AppCompatActivity() {

    private var gameId : String = ""
    private var maxPlayers: Int = 0
    private var nickname : String = ""
    private var questionCountTotal = 0

    private var startGameEnable : Boolean = false

    private var job = Job()
    override val coroutineContext = Dispatchers.Main + job

    private val context = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attempt_quiz_start)

        Client.withHandlers(
                mapOf( MsgQuestion ::class to { conn, msg -> showQuizActivity((msg as MsgQuestion).question as ChoiceQuestion)},
                        MsgPlayerCount ::class to { conn, msg -> updateProgressBar((msg as MsgPlayerCount).playerCount)}))

        gameId = intent.getStringExtra("gameId")
        var createdBy = intent.getStringExtra("createdBy")
        nickname = intent.getStringExtra("nickname")
        var gameName = intent.getStringExtra("gameName")
        maxPlayers = intent.getIntExtra("maxPlayers",0)
        questionCountTotal = intent.getIntExtra("questionCountTotal",0)
        startGameEnable = intent.getBooleanExtra("startEnable", false)
        val playerCount = intent.getIntExtra("playerCount", 0)

        updateUI(nickname, createdBy, gameName, questionCountTotal)
        updateProgressBar(playerCount + 1)

        btn_leave.setOnClickListener {
            sendMsgLeaveGame()
        }

    }


    fun sendMsgStartGame() {
        Client.send(MsgStartGame())
    }

    fun sendMsgLeaveGame() {
        Client.send(MsgLeave())
        launch { context.finish() }
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


    fun showQuizActivity(question: ChoiceQuestion) = launch{
        // Create an Intent to start the AllGamesActivity

        println("test : question ist da")
        val intent = Intent(context, QuizActivity::class.java)
        intent.putExtra("gameId" , gameId)
        intent.putExtra("questionCountTotal" , questionCountTotal)

        //put 1st question
        intent.putExtra("questionText" , question.text)
        intent.putExtra("correctChoice" , question.correctChoice)
        var answers = ArrayList<String>()
        answers.add(question.choices[0])
        answers.add(question.choices[1])
        answers.add(question.choices[2])

        intent.putStringArrayListExtra("answers", answers)
        intent.putExtra("Category", question.category.name)

        startActivity(intent)
        context.finish()
    }


    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
