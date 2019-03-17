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
import org.quizfight.common.MASTER_PORT
import org.quizfight.common.messages.*

class CreateGameActivity : CoroutineScope, AppCompatActivity() {

    private var job = Job()
    override val coroutineContext = Dispatchers.Main + job

    private var context = this
    private var nickname:String = " "

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
        }else if(ed_number_question.text.toString().toInt() > 20 ||
                ed_number_question.text.toString().toInt() < 1){
            ed_number_question.setError(getString(R.string.no_question_not_correct))
            focusView = ed_number_question
            cancel = true
        }

        if(TextUtils.isEmpty(ed_number_player.text)){
            ed_number_player.setError(getString(R.string.required))
            focusView = ed_number_player
            cancel = true
        }else if(ed_number_player.text.toString().toInt() > 8 ||
                ed_number_player.text.toString().toInt() < 2){
            ed_number_player.setError(getString(R.string.no_player_not_correct))
            focusView = ed_number_question
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
            launch {
                Client.withHandlers(mapOf(
                        MsgGameInfo ::class to { _, msg -> showAttemptQuizStartActivity((msg as MsgGameInfo).game) }
                ))
                Client.send(MsgCreateGame(gameRequest, nickname))
                btn_create_game.isEnabled = false
            }

        }
    }


    fun showAttemptQuizStartActivity(gameInfo: GameData) = launch{

        val intent = Intent(context,  AttemptQuizStartActivity::class.java)

        //send game's info to AttemptQuizStartActivity
        intent.putExtra("gameId" , gameInfo.id)
        intent.putExtra("maxPlayers" , gameInfo.maxPlayers)
        intent.putExtra("questionCountTotal" , gameInfo.questionCount)
        intent.putExtra("gameName" , gameInfo.name)
        intent.putExtra("nickname" , nickname)
        intent.putExtra("createdBy" , nickname)
        intent.putExtra("startEnable", true)
        intent.putExtra("creator", gameInfo.gameCreator)

        startActivity(intent)

        context.finish()

    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }



}
