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
import kotlinx.android.synthetic.main.layout_alert_enter_nickname.*
import kotlinx.coroutines.launch
import org.quizfight.common.SocketConnection
import org.quizfight.common.messages.MsgGameInfo
import org.quizfight.common.messages.MsgGameList
import org.quizfight.common.messages.MsgJoin
import org.quizfight.common.messages.MsgTransferToGameServer
import java.net.Socket

class GameDetailActivity : CoroutineScope, AppCompatActivity() {

    private var questionCountTotal : Int = 5
    private var gameId : String = ""
    private lateinit var nickname: String
    private var gameName = ""
    private var maxPlayers = 0

    private var nicknameEntered: Boolean = false

    private var job = Job()
    override val coroutineContext = Dispatchers.Main + job
    private lateinit var conn : SocketConnection

    private var context = this

    private var masterServerIp = ""
    private var gameServerIp = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_detail)

        masterServerIp = intent.getStringExtra("masterServerIP")

        updateUi()

        btn_join.setOnClickListener {
            if (nicknameEntered == false) {
                displayAlert()
            } else {
                showAttemptQuizStart()
            }
        }

    }


    fun updateUi(){

        questionCountTotal = intent.getIntExtra("questionCountTotal",0 )
        gameId = intent.getStringExtra("gameId" )
        gameName = intent.getStringExtra("gameName")
        maxPlayers = intent.getIntExtra("maxPlayers", 0 )

        text_view_actual_no_players.text = ""+ maxPlayers
        text_view_actual_no_questions.text = ""+ questionCountTotal
        text_view_game_name.text = gameName
    }


    fun showAttemptQuizStart() = launch{

        val intent = Intent(context, AttemptQuizStartActivity::class.java)

        //send game's info to AttemptQuizStartActivity
        intent.putExtra("gameId" , gameId)
        intent.putExtra("maxPlayers" , maxPlayers)
        intent.putExtra("questionCountTotal" , questionCountTotal)
        intent.putExtra("gameName" , gameName)
        intent.putExtra("nickname" , nickname)
        intent.putExtra("createdBy" , "")
        intent.putExtra("startEnable", false)
        intent.putExtra("masterServerIP", masterServerIp)
        intent.putExtra("gameServerIP", gameServerIp)

        startActivity(intent)
        context.finish()
    }


    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    fun displayAlert() {
        val builder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.layout_alert_enter_nickname, null)
        val editText = view.findViewById<View>(R.id.enter_nickname) as EditText
        builder.setView(view)

        builder.setPositiveButton(R.string.btn_confirm) { dialog, x ->
            nickname = editText.text.toString()
            if(!nickname.isNullOrBlank()) {
                nicknameEntered = true
                sendJoinMessage()
                showAttemptQuizStart()
            } else {
                displayAlert()
            }
        }

        builder.create().show()
    }

    fun sendJoinMessage() {
        launch(Dispatchers.IO) {
            conn = SocketConnection(Socket(masterServerIp, 34567),
                    mapOf(MsgTransferToGameServer ::class to { conn, msg -> transferToGameServer((msg as MsgTransferToGameServer))}) )
            conn.send(MsgJoin(gameId, nickname))

        }
    }

    fun transferToGameServer(msg : MsgTransferToGameServer){
        gameServerIp = msg.gameServer.ip
        println("gameServer : " + gameServerIp)
        launch(Dispatchers.IO) {
            conn = SocketConnection(Socket(gameServerIp, 4567), mapOf() )
            conn.send(MsgJoin(gameId, nickname))
        }
        showAttemptQuizStart()
    }
}
