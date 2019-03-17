package org.quizfight.quizfight

import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.support.v7.app.AlertDialog
import android.widget.Button
import kotlinx.android.synthetic.main.activity_quiz.*
import android.view.View
import android.widget.SeekBar
import kotlinx.coroutines.*
import org.quizfight.common.messages.*
import org.quizfight.common.question.Category
import org.quizfight.common.question.ChoiceQuestion
import android.widget.TableRow
import android.widget.TextView
import org.quizfight.common.question.GuessQuestion
import android.view.animation.AnimationUtils
import org.quizfight.quizfight.R.id.radio_group
import java.util.*


class QuizActivity : CoroutineScope, AppCompatActivity() {

    // The code in a "launch" block will run on the main thread.
    // Use launch{} whenever you want to change UI elements.
    private var job = Job()
    override val coroutineContext = Dispatchers.Main + job

    private var questionCounter: Int = 0
    private var questionCountTotal: Int = 0
    private lateinit var currentQuestion: MsgQuestion
    private var choiceQuestionAnswer = ""
    private var guessQuestionAnswer : Int = 0
    private var nickname :String = ""
    private var finalScore : Int = 0

    //Countdown Timer
    var millisInFuture: Long = 21000 // for 20 seconds plus 1 second imprecision
    val countDownInterval: Long = 1000 // sets the countdown interval to 1 second
    lateinit var timer : CountDownTimer

    private var rowList = listOf<TableRow>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        nickname = intent.getStringExtra("nickname")

                //Display 1st question
        questionCountTotal = intent.getIntExtra("questionCountTotal", 4)

        progress_question.max = millisInFuture.toInt()

        val questiontext = intent.getStringExtra("questionText")
        val category = intent.getStringExtra("Category")
        val isChoicheQuestion = intent.getBooleanExtra("isChoiceQuestion",false)

        if(isChoicheQuestion){
            val answers = intent.getStringArrayListExtra("answers")
            val correct = intent.getStringExtra("correctChoice")
            showNextQuestion(MsgQuestion(ChoiceQuestion(questiontext,
                    Category.valueOf(category) ,
                    listOf<String>(correct, answers[0], answers[1], answers[2]),
                    correct)))
        }else{
            val highest = intent.getIntExtra("highest",100)
            val lowest = intent.getIntExtra("lowest",0)
            val correct = intent.getIntExtra("correctchoice",10)
            showNextQuestion(MsgQuestion(GuessQuestion(questiontext,
                    Category.valueOf(category), lowest, highest, correct)))
        }


        Client.withHandlers(mapOf(
                MsgQuestion ::class to { _, msg -> showNextQuestion((msg as MsgQuestion))},
                MsgRanking::class to { _, msg -> showRanking(msg as MsgRanking)},
                MsgGameOver::class to { _, msg -> finishQuiz()},
                MsgConnectionLost::class to { _, msg -> displayDisconnectedPoll(msg as MsgConnectionLost)}
                ))

        rowList = listOf<TableRow>(table_row_first, table_row_second, table_row_third,
                table_row_fourth, table_row_fifth, table_row_sixth, table_row_seventh, table_row_eight)

        timer = timer(millisInFuture, countDownInterval)
    }


    override fun onDestroy() {
        super.onDestroy()
        job.cancel() // Kills the coroutine when the activity gets destroyed
    }

    //  = launch {} tells the function to run in the main thread if not stated otherwise
    // use = launch {} whenever the function could possibly be called from a non main thread.
    // for example all message handlers call from non main threads!
    fun showNextQuestion(question: MsgQuestion) = launch() {
        rowList.forEach({tr -> hideTableRows(tr)})
        showHide(question_outer_layout)
        showHide(score_outer_layout)

        currentQuestion = question

        if (questionCounter < questionCountTotal) {
            if (question.question is ChoiceQuestion) {
                showChoiceQuestion(question.question as ChoiceQuestion)
            } else {
                showGuessQuestion(question.question as GuessQuestion)
            }

            questionCounter++

            text_view_question_count.text = ("Question: " + questionCounter
                    + "/" + questionCountTotal)

            timer.cancel()
            timer = timer(millisInFuture, countDownInterval)
            timer.start()
        }else {
            finishQuiz()
        }
    }


    fun checkAnswer(view: View) = launch {
        val selectedButton: Button = view as Button
        choiceQuestionAnswer = view.text.toString()
        sendScore()
        if (choiceQuestionAnswer == (currentQuestion.question as ChoiceQuestion).correctChoice) {
            selectedButton.setBackgroundColor(Color.GREEN)
        } else {
            selectedButton.setBackgroundColor(Color.RED)
        }

        //disable Buttons
        btn_answer1.isEnabled = false
        btn_answer2.isEnabled = false
        btn_answer3.isEnabled = false
        btn_answer3.isEnabled = false

        Thread.sleep(2000)
    }


    fun finishQuiz() {
        score_titel_view.text = "GAME OVER"
        tv_your_score.visibility = View.VISIBLE
        tv_your_score.text = "Your score: $finalScore"
        val a = AnimationUtils.loadAnimation(this, R.anim.anim_game_over)
        a.reset()

        score_titel_view.clearAnimation()
        score_titel_view.startAnimation(a)

        Client.reconnectToMaster()
    }


    fun sendScore() {
        var timeLeft: Int = text_view_countdown.text.toString().toInt()
        if(currentQuestion.question is ChoiceQuestion){
            Client.send(MsgScore((currentQuestion.question as ChoiceQuestion).evaluate(choiceQuestionAnswer,timeLeft, 21)))

        }else{
            Client.send(MsgScore((currentQuestion.question as GuessQuestion).evaluate(guessQuestionAnswer,timeLeft, 21)))
        }

    }

    private fun timer(millisInFuture: Long, countDownInterval: Long): CountDownTimer  {
        return object: CountDownTimer(millisInFuture, countDownInterval) {
            override fun onTick(millisUntilFinished: Long) {
                //updateCountdownText(millisUntilFinished)
                progress_question.progress = millisInFuture.toInt() - millisUntilFinished.toInt()

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

    private fun showRanking(msg : MsgRanking) {
        launch{
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

                if(value.key.equals(nickname)){
                    finalScore = value.value
                }
            }
        }
        Thread.sleep(5000)
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


    fun showChoiceQuestion(question: ChoiceQuestion){

        guess_container.visibility = View.GONE
        answer_container.visibility = View.VISIBLE

        //reset all buttons
        btn_answer1.setBackgroundColor(Color.WHITE)
        btn_answer2.setBackgroundColor(Color.WHITE)
        btn_answer3.setBackgroundColor(Color.WHITE)
        btn_answer4.setBackgroundColor(Color.WHITE)

        btn_answer1.isEnabled = true
        btn_answer2.isEnabled = true
        btn_answer3.isEnabled = true
        btn_answer4.isEnabled = true

        var answerList: MutableList<String> = mutableListOf(question.correctChoice)
        answerList.addAll(question.choices)
        answerList.shuffle()

        question_text_view.text = question.text
        btn_answer1.text = answerList[0]
        btn_answer2.text = answerList[1]
        btn_answer3.text = answerList[2]
        btn_answer4.text = answerList[4]


    }

    fun showGuessQuestion(question: GuessQuestion){

        guess_container.visibility = View.VISIBLE
        answer_container.visibility = View.GONE

        question_text_view.text = question.text
        tv_lowest.text = "" + question.lowest
        tv_highest.text = "" + question.highest

        seekbar.min = question.lowest
        seekbar.max = question.highest
        seekbar.progress = question.highest/2

        tv_answer.setTextColor(Color.WHITE)
        btn_ok.visibility = View.GONE
        btn_ok.isEnabled = true

        btn_ok.setOnClickListener {
            sendScore()
            btn_ok.isEnabled = false
            if(guessQuestionAnswer == question.correctValue){
                tv_answer.setTextColor(Color.GREEN)
            }else{
                tv_answer.setTextColor(Color.RED)
            }
        }

        seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                // Display the current progress of SeekBar
                tv_answer.text = "Your answer : $i"
                guessQuestionAnswer = i

            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                btn_ok.visibility = View.VISIBLE

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
            }
        })

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
