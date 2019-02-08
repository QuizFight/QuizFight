package org.quizfight.quizfight

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import kotlinx.android.synthetic.main.activity_create_game.*
import org.quizfight.common.question.Question

class CreateGameActivity : AppCompatActivity() {

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
        ed_nickname.setError(null)
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
            //create Game
            // TODO send msg to server
            val game : Game


            val intent = Intent(this,  GameDetailActivity::class.java)

            //send game's info to GameDetailActivity
            //TODO read info from received game
            intent.putExtra("gameName" , ed_game_name.text)
            intent.putExtra("questionCountTotal" , ed_number_question.text)
            intent.putExtra("maxPlayer" , ed_number_player.text)
            intent.putExtra("nickname" , ed_nickname.text)
            intent.putExtra("gameId" , 1)

            startActivity(intent)
            this.finish()
        }
    }
}

data class Game(val id: Int, val gameName:String, val maxPlayer: Int, var questions: List<Question>)