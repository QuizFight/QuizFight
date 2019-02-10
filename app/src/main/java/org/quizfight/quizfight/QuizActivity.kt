package org.quizfight.quizfight

import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import kotlinx.android.synthetic.main.activity_quiz.*
import org.quizfight.common.question.FourAnswersQuestion
import android.view.View
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.quizfight.common.messages.MsgSendAnswer


class QuizActivity : AppCompatActivity() {
    private var questionCounter: Int = 0
    private var questionCountTotal: Int = 0

    private lateinit var currentQuestion: FourAnswersQuestion
    private lateinit var client : Client

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        //sollte auch vom Server gelesen werden
        questionCountTotal = intent.getIntExtra("questionCountTotal", 0)

        Thread(Runnable {
            client = Client("10.0.2.2", 34567, this)
        }).start()

    }

    fun showNextQuestion(question: FourAnswersQuestion) {
       if (questionCounter < questionCountTotal) {
           currentQuestion = question
           var answerList: MutableList<String> = mutableListOf(currentQuestion.correctAnswer,
                   currentQuestion.badAnswer_1,
                   currentQuestion.badAnswer_2,
                   currentQuestion.badAnswer_3)
           answerList.shuffle()

           this@QuizActivity.runOnUiThread(java.lang.Runnable {

                text_view_question.text = currentQuestion.text
                answer_button1.text = answerList[0]
                answer_button2.text = answerList[1]
                answer_button3.text = answerList[2]
                answer_button4.text = answerList[3]

                questionCounter++


                text_view_question_count.text = ("Question: " + questionCounter
                        + "/" + questionCountTotal)
           })
       } else {
            finishQuiz()
       }
    }

    fun checkAnswer(view: View) {
        val selectedButton: Button = findViewById(radio_group.checkedRadioButtonId)
        val answer: CharSequence = selectedButton.text
        if (answer == currentQuestion.correctAnswer) {
            selectedButton.setTextColor(Color.GREEN)
        } else {
            selectedButton.setTextColor(Color.WHITE)
        }

        GlobalScope.launch{
            client.conn.send(MsgSendAnswer(0, 10))
        }
    }

    fun finishQuiz() {
        finish()
    }
}
