package org.quizfight.quizfight

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_start.*
import kotlinx.coroutines.launch
import org.quizfight.common.messages.MsgQuestion
import org.quizfight.common.question.ChoiceQuestion
import org.quizfight.common.question.GuessQuestion
import java.util.ArrayList


/**
 * This activity is the first activity of the app
 * It proposes two button to create a game or to join a game
 * @author Aude Nana
 */
class StartActivity : AppCompatActivity() {

    private var masterServerIp = "192.168.0.30"
    private var gameId = ""
    private var nickname = ""
    private var context = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_start)

      /*  if(readGamesInfo()){
            Client.send(MsgRejoin(nickname, gameId)
            val intent = Intent(this, QuizActivity::class.java)

        }*/
    }

    /**
     * This methods shows the AllGamesActivity ,
     * where all open games are displayed
     */
    fun showAllGamesActivity(view: View) {
        val intent = Intent(this, AllGamesActivity::class.java)
        if(!ed_masterServerIp.text.toString().isEmpty())
            masterServerIp = ed_masterServerIp.text.toString()
        intent.putExtra("masterServerIP", masterServerIp)
        startActivity(intent)

    }

    /**
     * This method shows the CreateGameActivity,
     * where the user can create a new Game
     */
    fun showCreateGameActivity(view: View) {
        val intent = Intent(this, CreateGameActivity::class.java)
        intent.putExtra("masterServerIP", masterServerIp)
        startActivity(intent)

    }

    fun readGamesInfo() : Boolean{
        val preferences = this.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        gameId = preferences.getString("gameId", "")
        nickname = preferences.getString("nickname", "")
        if(!gameId.isEmpty()){
            return true
        }
        return false
    }

   /* fun showQuizActivity(msg: MsgQuestion) = launch{

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
        context.finish()
    }*/


}
