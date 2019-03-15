package org.quizfight.quizfight

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.support.v7.app.AlertDialog
import android.widget.Button
import kotlinx.android.synthetic.main.activity_quiz.*
import android.view.View
import kotlinx.coroutines.*
import org.quizfight.common.messages.*
import org.quizfight.common.question.Category
import org.quizfight.common.question.ChoiceQuestion
import java.util.Locale
import android.widget.TableRow
import android.widget.TextView

class QuizActivity : CoroutineScope, AppCompatActivity() {

    // The code in a "launch" block will run on the main thread.
    // Use launch{} whenever you want to change UI elements.
    private var job = Job()
    override val coroutineContext = Dispatchers.Main + job

    private var questionCounter: Int = 0
    private var questionCountTotal: Int = 0

    private lateinit var currentQuestion: ChoiceQuestion
    private var answerSelected : Boolean = false


    //Countdown Timer
    val millisInFuture: Long = 21000 // for 20 seconds plus 1 second imprecision
    val countDownInterval: Long = 1000 // sets the countdown interval to 1 second

    val rowList = listOf<TableRow>(table_row_first, table_row_second, table_row_third,
            table_row_fourth, table_row_fifth, table_row_sixth, table_row_seventh, table_row_eight)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        //sollte auch vom Server gelesen werden
        questionCountTotal = intent.getIntExtra("questionCountTotal", 4)

        val questiontext = intent.getStringExtra("questionText")
        val correct = intent.getStringExtra("correctChoice")
        val answers = intent.getStringArrayListExtra("answers")
        val category = intent.getStringExtra("Category")

        showNextQuestion(MsgQuestion(ChoiceQuestion(questiontext,
                Category.valueOf(category) ,
                listOf<String>(correct, answers[0], answers[1], answers[2]),
                correct)))

        Client.withHandlers(mapOf(
                MsgQuestion ::class to { _, msg -> showNextQuestion((msg as MsgQuestion))},
                MsgRanking::class to { _, msg -> showRanking(msg as MsgRanking)},
                MsgGameInfo::class to { _, msg -> finishQuiz()}
        ))
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
            rowList.forEach({tr -> hideTableRows(tr)})
            showHide(question_outer_layout)
            showHide(score_outer_layout)

            if (question.question is ChoiceQuestion) {
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
            }else {

            }
        }
    }

    //ob user geantwortet hat oder nicht
    fun checkAnswer(view: View) {
        answerSelected = true
    }


    fun finishQuiz() {
        finish()
        Client.reconnectToMaster()
    }


    fun sendScore() = launch{
        var answer: String = " "
        if(answerSelected) {
            val selectedButton: Button = findViewById(radio_group.checkedRadioButtonId)
            answer = selectedButton.text.toString()
        }

        Client.send(MsgScore(currentQuestion.evaluate(answer)))

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
        showHide(question_outer_layout)
        showHide(score_outer_layout)

        val rowNicknameScoreViews = listOf<Triple<TableRow, TextView, TextView>>(
                Triple(table_row_first, nickname_view1, score_view1), Triple(table_row_second, nickname_view2, score_view2),
                Triple(table_row_third, nickname_view3, score_view3), Triple(table_row_fourth, nickname_view4, score_view4),
                Triple(table_row_fifth, nickname_view4, score_view4), Triple(table_row_sixth, nickname_view6, score_view6),
                Triple(table_row_seventh, nickname_view7, score_view7), Triple(table_row_eight, nickname_view8, score_view8)
        )
        val iter = msg.totalScore.iterator()

        for((index, value) in iter.withIndex()) {
            showHide(rowNicknameScoreViews[index].first)
            rowNicknameScoreViews[index].second.text = value.key
            rowNicknameScoreViews[index].third.text = value.value.toString()

        }
    }

    fun showHide(view: View) {
        view.visibility =
                if(view.visibility == View.VISIBLE) {
                    View.GONE
                } else {
                    View.VISIBLE
                }
    }

    fun hideTableRows(tableRow: TableRow) {
        tableRow.visibility =
                if(tableRow.visibility == View.VISIBLE) {
                    View.GONE
                } else {
                    View.GONE
                }
    }

    fun displayDisconnectedPoll(msg: MsgConnectionLost) {
        val builder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.layout_disconnect_poll, null)
        builder.setView(view)

        builder.setPositiveButton("wait") { _, _ ->
            Client.send(MsgVote(waitForPlayer = true, name = msg.name))

        }

        builder.setNegativeButton("don't wait") { _, _ ->
            Client.send(MsgVote(waitForPlayer = false, name = msg.name))

        }

        builder.create().show()
    }
}
