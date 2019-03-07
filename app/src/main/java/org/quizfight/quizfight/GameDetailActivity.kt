package org.quizfight.quizfight

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_game_detail.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.quizfight.common.messages.MsgJoin

class GameDetailActivity : CoroutineScope, AppCompatActivity() {

    private var questionCountTotal : Int = 5
    private var gameId : Int = 0
    private var nickname = ""
    private var gameName = ""
    private var maxPlayers = 0

    private var job = Job()
    override val coroutineContext = Dispatchers.Main + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_detail)
        updateUi()

        btn_join.setOnClickListener {
            showAttemptQuizStart()
        }
    }


    fun updateUi(){

        questionCountTotal = intent.getIntExtra("questionCountTotal",0 )
        gameId = intent.getIntExtra("gameId",0 )
        gameName = intent.getStringExtra("gameName")
        maxPlayers = intent.getIntExtra("maxPlayers", 0 )

        text_view_actual_no_players.text = ""+ maxPlayers
        text_view_actual_no_questions.text = ""+ questionCountTotal
        text_view_game_name.text = gameName
    }


    fun showAttemptQuizStart(){

      /*  launch(Dispatchers.IO) {
            val client = Client("10.0.2.2", 34567, this@GameDetailActivity)
            client.conn.send(MsgJoin(gameId,"someone"))
        }*/

        val intent = Intent(this, AttemptQuizStartActivity::class.java)

        //send game's info to AttemptQuizStartActivity
        intent.putExtra("gameId" , gameId)
        intent.putExtra("maxPlayers" , maxPlayers)
        intent.putExtra("questionCountTotal" , questionCountTotal)
        intent.putExtra("gameName" , gameName)
        intent.putExtra("nickname" , nickname)
        intent.putExtra("createdBy" , "")

        startActivity(intent)
        this.finish()
    }


    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

}
