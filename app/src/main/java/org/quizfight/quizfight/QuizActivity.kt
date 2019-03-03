package org.quizfight.quizfight

import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import kotlinx.android.synthetic.main.activity_quiz.*
import android.view.View
import kotlinx.coroutines.*
import org.quizfight.common.question.ChoiceQuestion
import kotlin.coroutines.CoroutineContext

class QuizActivity : CoroutineScope, AppCompatActivity() {

    // The code in a "launch" block will run on the main thread.
    // Use launch{} whenever you want to change UI elements.
    private var job = Job()
    override val coroutineContext = Dispatchers.Main + job

    private var questionCounter: Int = 0
    private var questionCountTotal: Int = 0

    private lateinit var currentQuestion: ChoiceQuestion
    private lateinit var client : Client

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        //sollte auch vom Server gelesen werden
        questionCountTotal = intent.getIntExtra("questionCountTotal", 5)

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


       } else {
            finishQuiz()
       }
    }

    fun checkAnswer(view: View) {

        val selectedButton: Button = findViewById(radio_group.checkedRadioButtonId)
        val answer: CharSequence = selectedButton.text
        if (answer == currentQuestion.correctChoice) {
            selectedButton.setTextColor(Color.GREEN)
        } else {
            selectedButton.setTextColor(Color.WHITE)
        }

        launch(Dispatchers.IO){
            currentQuestion.evaluate(answer.toString())
        }
    }

    fun finishQuiz() {
        finish()
    }
}
