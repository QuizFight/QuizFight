package org.quizfight.quizfight

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import kotlinx.android.synthetic.main.activity_quiz.*
import android.view.View
import kotlinx.coroutines.*
import org.quizfight.common.messages.MsgJoin
import org.quizfight.common.messages.MsgScore
import org.quizfight.common.question.ChoiceQuestion
import java.util.Locale

class QuizActivity : CoroutineScope, AppCompatActivity() {

    // The code in a "launch" block will run on the main thread.
    // Use launch{} whenever you want to change UI elements.
    private var job = Job()
    override val coroutineContext = Dispatchers.Main + job

    private var questionCounter: Int = 0
    private var questionCountTotal: Int = 0

    private lateinit var currentQuestion: ChoiceQuestion
    private var answerSelected : Boolean = false
    private lateinit var client : Client

    //Countdown Timer
    val millisInFuture: Long = 21000 // for 20 seconds plus 1 second imprecision
    val countDownInterval: Long = 1000 // sets the countdown interval to 1 second

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        //sollte auch vom Server gelesen werden
        questionCountTotal = intent.getIntExtra("questionCountTotal", 4)

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
    // use = launch {} whenever the function could possibly be called from a non main thread.
    // for example all message handlers call from non main threads!
    fun showNextQuestion(question: ChoiceQuestion) = launch {

        //reset all selected buttons
        radio_group.clearCheck()

       if (questionCounter < questionCountTotal) {
           currentQuestion = question
           var answerList: MutableList<String> = mutableListOf(currentQuestion.correctChoice)
           answerList.addAll(currentQuestion.choices)
           answerList.shuffle()

           text_view_question.text = currentQuestion.text
           answer_button1.text = answerList[0]
           answer_button2.text = answerList[1]
           answer_button3.text = answerList[2]
           answer_button4.text = answerList[3]

           questionCounter++

           text_view_question_count.text = ("Question: " + questionCounter
                       + "/" + questionCountTotal)

           timer(millisInFuture, countDownInterval).start()

       } else {
            finishQuiz()
       }
    }

    //ob user geantwortet hat oder nicht
    fun checkAnswer(view: View) {
        answerSelected = true
    }


    fun finishQuiz() {
        finish()
    }


    fun sendScore(){
        var answer: String = " "
        if(answerSelected) {
            val selectedButton: Button = findViewById(radio_group.checkedRadioButtonId)
            answer = selectedButton.text.toString()
        }

        launch(Dispatchers.Default){
            client.conn.send(MsgScore(currentQuestion.evaluate(answer.toString())))
        }

        answerSelected = false
    }

    private fun timer(millisInFuture: Long, countDownInterval: Long): CountDownTimer {
        return object: CountDownTimer(millisInFuture, countDownInterval) {
            override fun onTick(millisUntilFinished: Long) {
                updateCountdownText(millisUntilFinished)
            }

            override fun onFinish() {
                TODO("not implemented")
            }
        }
    }

    private fun updateCountdownText(timeLeft: Long) {
        val seconds: Int = (timeLeft / 1000).toInt()
        text_view_countdown.text = String.format(Locale.getDefault(), "%02d", seconds)
    }
}
