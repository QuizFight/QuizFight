package org.quizfight.quizfight

import android.graphics.Color
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import kotlinx.android.synthetic.main.activity_quiz.*
import org.quizfight.common.question.FourAnswersQuestion
import org.quizfight.common.question.Question
import android.view.View

class QuizActivity : AppCompatActivity() {
    var questionCounter: Int = 0
    var questionCountTotal: Int = 0

    var questionList: MutableList<FourAnswersQuestion> = mutableListOf(FourAnswersQuestion("Whomst've done it?",
                                            "Geography", "fourAnswerQuestion",
                                            "Herr Prozessor Dr. Schäfer",
                                            "Captain A.", "Shrimp Man",
                                            "Kapitalism"))
    var currentQuestion: FourAnswersQuestion = questionList[0]
    var answerList: MutableList<String> = mutableListOf(currentQuestion.correctAnswer, currentQuestion.badAnswer_1,
                                                        currentQuestion.badAnswer_2, currentQuestion.badAnswer_3)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        questionCountTotal = questionList.size

        ShowQuestionTask(this).execute()
       // showNextQuestion()
    }

    fun showNextQuestion(question: FourAnswersQuestion) {
     //   if (questionCounter < questionCountTotal) {

            answerList.shuffle()

            text_view_question.text = question.text
            answer_button1.text = question.badAnswer_1
            answer_button2.text = answerList[1]
            answer_button3.text = answerList[2]
            answer_button4.text = answerList[3]

            questionCounter++

            text_view_question_count.text = ("Question: " + questionCounter
                    + "/" + questionCountTotal)
     /*   } else {
            finishQuiz()
        }*/
    }

    fun checkAnswer(view: View) {
        val selectedButton: Button = findViewById(radio_group.checkedRadioButtonId)
        val answer: CharSequence = selectedButton.text
        if (answer == currentQuestion.correctAnswer) {
            selectedButton.setTextColor(Color.GREEN)
        } else {
            selectedButton.setTextColor(Color.WHITE)
        }
    }

    fun finishQuiz() {
        finish()
    }
}

class ShowQuestionTask(private var activity: QuizActivity?) : AsyncTask<String, String, String>() {

    override fun onPreExecute() {
        super.onPreExecute()
    }

    override fun doInBackground(vararg p0: String?): String {



        var result = ""
        try {
            val client = Client("10.0.2.2", 12345, activity)

        } catch (ex: Exception) {
            Log.d("", "Error in doInBackground " + ex.message)
        }
        return result
    }

}