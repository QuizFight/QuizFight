package org.quizfight.quizfight

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_game_detail.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.EditText
import kotlinx.coroutines.launch
import org.quizfight.common.messages.*

class GameDetailActivity : CoroutineScope, AppCompatActivity() {

    private var job = Job()
    override val coroutineContext = Dispatchers.Main + job

    private var context = this

    private var questionCountTotal = 0
    private var gameId = ""
    private lateinit var nickname: String
    private var gameName = ""
    private var maxPlayers = 0
    private var playerCount = 0
    private var creator = ""

    private var nicknameEntered = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_detail)
        updateUi()

        //enter nickname
        btn_join.setOnClickListener {
            if (nicknameEntered == false) {
                displayAlert()
            } else {
                showAttemptQuizStart()
            }
        }

    }


    /**
     * Update UI
     */
    fun updateUi(){

        questionCountTotal = intent.getIntExtra("questionCountTotal",0 )
        gameId = intent.getStringExtra("gameId" )
        gameName = intent.getStringExtra("gameName")
        maxPlayers = intent.getIntExtra("maxPlayers", 0 )
        playerCount= intent.getIntExtra("playerCount",0)
        creator = intent.getStringExtra("creator")

        text_view_actual_no_players.text = ""+ maxPlayers
        text_view_actual_no_questions.text = ""+ questionCountTotal
        text_view_game_name.text = gameName
        text_view_creator_name.text = creator
    }


    /**
     * switch to AttemptQuizStartActivity after join
     */
    fun showAttemptQuizStart() = launch{

        val intent = Intent(context, AttemptQuizStartActivity::class.java)

        //send game's info to AttemptQuizStartActivity
        intent.putExtra("gameId" , gameId)
        intent.putExtra("maxPlayers" , maxPlayers)
        intent.putExtra("questionCountTotal" , questionCountTotal)
        intent.putExtra("gameName" , gameName)
        intent.putExtra("nickname" , nickname)
        intent.putExtra("creator" , creator)
        intent.putExtra("startEnable", false)
        intent.putExtra("playerCount", playerCount)

        startActivity(intent)
        context.finish()
    }


    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }


    /**
     * displays dialog to enter the nickname
     */
    fun displayAlert() {
        val builder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.layout_alert_enter_nickname, null)
        val editText = view.findViewById<View>(R.id.enter_nickname) as EditText
        builder.setView(view)

        builder.setPositiveButton(R.string.btn_confirm) { dialog, x ->
            nickname = editText.text.toString()
            if(!nickname.isNullOrBlank()) {
                nicknameEntered = true
                Client.withHandlers(mapOf(
                        MsgPlayerCount ::class to { _, msg -> }
                ))
                Client.send(MsgJoin(gameId, nickname))
                showAttemptQuizStart()
            } else {
                displayAlert()
            }
        }

        builder.create().show()
    }
}
