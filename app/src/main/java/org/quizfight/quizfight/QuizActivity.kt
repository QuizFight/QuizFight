package org.quizfight.quizfight

import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import kotlinx.android.synthetic.main.activity_quiz.*
import org.quizfight.common.question.FourAnswersQuestion
import android.view.View
import kotlinx.coroutines.*
import org.quizfight.common.messages.MsgSendAnswer
import kotlin.coroutines.CoroutineContext

class QuizActivity : CoroutineScope, AppCompatActivity() {

    // The code in a "launch" block will run on the main thread.
    // Use launch{} whenever you want to change UI elements.
    private var job = Job()
    override val coroutineContext = Dispatchers.Main + job

    private var questionCounter: Int = 0
    private var questionCountTotal: Int = 0

    private lateinit var currentQuestion: FourAnswersQuestion
    private lateinit var client : Client

    private var answerSelected : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        //TODO : read questionCountTotal from Server
        questionCountTotal = intent.getIntExtra("questionCountTotal", 0)

        // Use launch(Dispatchers.IO){} for networking operations
        launch(Dispatchers.IO) {
            client = Client("10.0.2.2", 34567, this@QuizActivity)
        }


    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel() // Kills the coroutine when the activity gets destroyed
    }

    //  = launch {} tells the function to run in the main thread if not stated otherwise
    // use = launch {} whenever the function could possibly be called from a non main thread
    // for example calls from message handlers!
    fun showNextQuestion(question: FourAnswersQuestion) = launch {

        //reset all selected buttons
        radio_group.clearCheck()

       if (questionCounter < questionCountTotal) {
           currentQuestion = question
           var answerList: MutableList<String> = mutableListOf(currentQuestion.correctAnswer,
                   currentQuestion.badAnswer_1,
                   currentQuestion.badAnswer_2,
                   currentQuestion.badAnswer_3)
           answerList.shuffle()

           text_view_question.text = currentQuestion.text
           answer_button1.text = answerList[0]
           answer_button2.text = answerList[1]
           answer_button3.text = answerList[2]
           answer_button4.text = answerList[3]

           questionCounter++

           text_view_question_count.text = ("Question: " + questionCounter
                       + "/" + questionCountTotal)

       } else {
            finishQuiz()
       }
    }

    fun checkAnswer(view: View) {
        answerSelected = true

    }

    fun finishQuiz() {
        finish()
    }

    fun sendScore(){

        var score : Int = 0

        if(answerSelected) {
            val selectedButton: Button = findViewById(radio_group.checkedRadioButtonId)
            val answer: CharSequence = selectedButton.text
            if (answer == currentQuestion.correctAnswer) {
                score = 10
            }

        }
        launch(Dispatchers.Default){
            client.conn.send(MsgSendAnswer(0, score))
        }

        answerSelected = false

    }

}
