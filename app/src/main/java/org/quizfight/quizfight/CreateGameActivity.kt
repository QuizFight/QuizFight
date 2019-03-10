package org.quizfight.quizfight

import android.support.v7.app.AppCompatActivity
import android.os.Bundle

import android.content.Intent
import android.text.TextUtils
import android.view.View
import kotlinx.android.synthetic.main.activity_create_game.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.quizfight.common.messages.GameRequest
import org.quizfight.common.messages.MsgCreateGame
import org.quizfight.common.messages.MsgGameInfo

class CreateGameActivity : CoroutineScope, AppCompatActivity() {

    private lateinit var client : Client
    private var job = Job()
    override val coroutineContext = Dispatchers.Main + job

    var nickname:String = " "

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_game)

        btn_create_game.setOnClickListener {
            validateForm()
        }


    }

    fun validateForm(){

        //reset errors
        ed_game_name.setError(null)
        ed_number_player.setError(null)
        ed_number_question.setError(null)

        var cancel : Boolean = false
        lateinit var focusView : View

        //check editViews
        if(TextUtils.isEmpty(ed_game_name.text)){
            ed_game_name.setError(getString(R.string.required))
            focusView = ed_game_name
            cancel = true
        }

        if(TextUtils.isEmpty(ed_number_question.text)){
            ed_number_question.setError(getString(R.string.required))
            focusView = ed_number_question
            cancel = true
        }

        if(TextUtils.isEmpty(ed_number_player.text)){
            ed_number_player.setError(getString(R.string.required))
            focusView = ed_number_player
            cancel = true
        }

        if(TextUtils.isEmpty(ed_nickname.text)){
            ed_nickname.setError(getString(R.string.required))
            focusView = ed_nickname
            cancel = true
        }

        //performs requestFocus or starts a new activity
        if(cancel){
            focusView.requestFocus()
        } else {
            //read games info
            var gameName = ed_game_name.text.toString()
            var maxPlayer =  ed_number_player.text.toString().toInt()
            var questionCount = ed_number_question.text.toString().toInt()
            nickname = ed_nickname.text.toString()

            val gameRequest = GameRequest(gameName, maxPlayer, questionCount)
            //create Game in Backend
            sendMsgCreateGameToServer(gameRequest,nickname )
        }
    }


    fun sendMsgCreateGameToServer(gameRequest: GameRequest, nickname: String){
        // Use launch(Dispatchers.IO){} for networking operations
        launch(Dispatchers.IO) {
            client = Client("192.168.0.32", 34567, this@CreateGameActivity)
            client.conn.send(MsgCreateGame(gameRequest,nickname))
        }
    }

    fun showAttemptQuizStartActivity(gameInfo: MsgGameInfo){
        val intent = Intent(this,  AttemptQuizStartActivity::class.java)

        //send game's info to AttemptQuizStartActivity
        intent.putExtra("gameId" , gameInfo.game.id)
        intent.putExtra("maxPlayers" , gameInfo.game.maxPlayers)
        intent.putExtra("questionCountTotal" , gameInfo.game.questionCount)
        intent.putExtra("gameName" , gameInfo.game.name)
        intent.putExtra("nickname" , nickname)
        intent.putExtra("createdBy" , nickname)
        intent.putExtra("startEnable", true)

        startActivity(intent)
        this.finish()
    }
}
