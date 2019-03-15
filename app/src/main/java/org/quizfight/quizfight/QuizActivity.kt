package org.quizfight.quizfight

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import kotlinx.android.synthetic.main.activity_quiz.*
import android.view.View
import kotlinx.coroutines.*
import org.quizfight.common.SocketConnection
import org.quizfight.common.messages.*
import org.quizfight.common.question.ChoiceQuestion
import java.net.Socket
import java.util.Locale

class QuizActivity : CoroutineScope, AppCompatActivity() {

    // The code in a "launch" block will run on the main thread.
    // Use launch{} whenever you want to change UI elements.
    private var job = Job()
    override val coroutineContext = Dispatchers.Main + job
    private lateinit var conn : SocketConnection

    private var questionCounter: Int = 0
    private var questionCountTotal: Int = 0

    private lateinit var currentQuestion: ChoiceQuestion
    private var answerSelected : Boolean = false

    //Countdown Timer
    val millisInFuture: Long = 21000 // for 20 seconds plus 1 second imprecision
    val countDownInterval: Long = 1000 // sets the countdown interval to 1 second

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        //sollte auch vom Server gelesen werden
        questionCountTotal = intent.getIntExtra("questionCountTotal", 4)

        launch(Dispatchers.IO) {
            conn = SocketConnection(Socket("10.0.2.2", 34567),
                    mapOf(MsgQuestion ::class to { conn, msg -> showNextQuestion((msg as MsgQuestion))} ,
                            MsgRanking::class to { conn, msg -> showRanking(msg as MsgRanking)} ))

        }


    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel() // Kills the coroutine when the activity gets destroyed
    }

    //  = launch {} tells the function to run in the main thread if not stated otherwise
    // use = launch {} whenever the function could possibly be called from a non main thread.
    // for example all message handlers call from non main threads!
    fun showNextQuestion(question: MsgQuestion) {
        launch() {

            //reset all selected buttons
            radio_group.clearCheck()

            if (questionCounter < questionCountTotal) {
                currentQuestion = question.question as ChoiceQuestion
                var answerList: MutableList<String> = mutableListOf(currentQuestion.correctChoice)
                answerList.addAll(currentQuestion.choices)
                answerList.shuffle()

                question_text_view.text = currentQuestion.text
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
    }

    //ob user geantwortet hat oder nicht
    fun checkAnswer(view: View) {
        answerSelected = true
    }


    fun finishQuiz() {
        finish()
    }


    fun sendScore() = launch{
        var answer: String = " "
        var timeLeft: Int = 0
        if(answerSelected) {
            val selectedButton: Button = findViewById(radio_group.checkedRadioButtonId)
            answer = selectedButton.text.toString()
            timeLeft = text_view_countdown.text.toString().toInt()
        }

        launch(Dispatchers.Default){
            conn.send(MsgScore(currentQuestion.evaluate(answer.toString(), timeLeft, 21)))
        }

        answerSelected = false
    }

    private fun timer(millisInFuture: Long, countDownInterval: Long): CountDownTimer  {
        return object: CountDownTimer(millisInFuture, countDownInterval) {
            override fun onTick(millisUntilFinished: Long) {
                updateCountdownText(millisUntilFinished)
            }

            override fun onFinish() {
                sendScore()

            }
        }
    }

    private fun updateCountdownText(timeLeft: Long) = launch{
        val seconds: Int = (timeLeft / 1000).toInt()
        text_view_countdown.text = String.format(Locale.getDefault(), "%02d", seconds)
    }

    private fun showRanking(msg : MsgRanking) = launch{

    }

    fun showHide(view: View) {
        view.visibility =
                if(view.visibility == View.VISIBLE) {
                    View.GONE
                } else {
                    View.VISIBLE
                }
    }
}
