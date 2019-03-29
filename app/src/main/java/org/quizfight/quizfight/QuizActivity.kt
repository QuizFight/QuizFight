package org.quizfight.quizfight

import android.content.Context
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
import java.util.*
import android.os.Handler



class QuizActivity : CoroutineScope, AppCompatActivity() {

    //Coroutine
    private var job = Job()
    override val coroutineContext = Dispatchers.Main + job

    private val context = this

    //Game's info
    private var gameId = ""
    private var questionCountTotal: Int = 0
    private var nickname :String = ""

    //current info
    private lateinit var currentQuestion: MsgQuestion
    private var choiceQuestionAnswer = ""
    private var guessQuestionAnswer : Int = 0
    private var finalScore : Int = 0
    private var hasVoted = false
    private var isGameOver = false

    //Countdown Timer
    var millisInFuture: Long = 21000 // for 20 seconds plus 1 second imprecision
    val countDownInterval: Long = 1000 // sets the countdown interval to 1 second
    lateinit var timer : CountDownTimer

    //Ranking
    private var rowList = listOf<TableRow>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        //read nickname and gameID from previous activity
        nickname = intent.getStringExtra("nickname")
        gameId = intent.getStringExtra("gameId")

        //update timer bar
        progress_timer.max = millisInFuture.toInt()

        //Display 1st question
        questionCountTotal = intent.getIntExtra("questionCountTotal", 4)

        val questiontext = intent.getStringExtra("questionText")
        val category = intent.getStringExtra("Category")
        val isChoicheQuestion = intent.getBooleanExtra("isChoiceQuestion",false)

        if(isChoicheQuestion){
            val answers = intent.getStringArrayListExtra("answers")
            val correct = intent.getStringExtra("correctChoice")
            showNextQuestion(MsgQuestion(ChoiceQuestion(questiontext,
                    Category.valueOf(category) ,
                    listOf<String>(answers[0], answers[1], answers[2],answers[3]),
                    correct), 1))
        }else{
            val highest = intent.getIntExtra("highest",100)
            val lowest = intent.getIntExtra("lowest",0)
            val correct = intent.getIntExtra("correctchoice",10)
            showNextQuestion(MsgQuestion(GuessQuestion(questiontext,
                    Category.valueOf(category), lowest, highest, correct), 1))
        }


        //Update handlers of client
        Client.withHandlers(mapOf(
                MsgQuestion ::class to { _, msg -> showNextQuestion((msg as MsgQuestion))},
                MsgRanking::class to { _, msg -> showRanking(msg as MsgRanking)},
                MsgGameOver::class to { _, _ -> finishQuiz()},
                MsgConnectionLost::class to { _, msg -> displayDisconnectedPoll(msg as MsgConnectionLost)},
                MsgCheckConnection::class to {_,_ -> },
                MsgWait::class to {_, _ -> displayWaitingForReconnection()},
                MsgPlayerCount::class to { _ , _ -> }
        ))

        //initialize UI elements
        rowList = listOf<TableRow>(table_row_first, table_row_second, table_row_third,
                table_row_fourth, table_row_fifth, table_row_sixth, table_row_seventh, table_row_eight)
        timer = timer(millisInFuture, countDownInterval)
    }


    /**
     * Displays a question
     */
    fun showNextQuestion(question: MsgQuestion) = launch {

        //show question view
        rowList.forEach({tr -> hideTableRows(tr)})
        score_outer_layout.visibility = View.GONE
        reconnection_outer_layout.visibility = View.GONE
        question_outer_layout.visibility = View.VISIBLE

        currentQuestion = question

        //check if question ChoiceQuestion or GuessQuestion is
        if (question.question is ChoiceQuestion) {
            showChoiceQuestion(question.question as ChoiceQuestion)
        } else {
            showGuessQuestion(question.question as GuessQuestion)
        }

        //update the question count textView
        text_view_question_count.text = ("Question: " + question.number
                + "/" + questionCountTotal)

        //if timer runs, cancel and start a new timer
        timer.cancel()
        timer = timer(millisInFuture, countDownInterval)
        timer.start()
    }


    /**
     * read answer of choiceQuestion, evaluate and send score to the game server
     */
    fun checkAnswer(view: View) = launch {

        //read view
        val selectedButton: Button = view as Button
        choiceQuestionAnswer = view.text.toString()

        sendScore()

        if (choiceQuestionAnswer == (currentQuestion.question as ChoiceQuestion).correctChoice) {
            selectedButton.setBackgroundColor(Color.GREEN)
        } else {
            val correctAnswer = (currentQuestion.question as ChoiceQuestion).correctChoice
            selectedButton.setBackgroundColor(Color.parseColor("#f50a0a"))
            when {
                btn_answer1.text == correctAnswer
                -> btn_answer1.setBackgroundColor(Color.GREEN)

                btn_answer2.text == correctAnswer
                -> btn_answer2.setBackgroundColor(Color.GREEN)

                btn_answer3.text == correctAnswer
                -> btn_answer3.setBackgroundColor(Color.GREEN)

                btn_answer4.text == correctAnswer
                -> btn_answer4.setBackgroundColor(Color.GREEN)
            }
        }

        //disable Buttons
        btn_answer1.isEnabled = false
        btn_answer2.isEnabled = false
        btn_answer3.isEnabled = false
        btn_answer4.isEnabled = false

        //update button style
        btn_answer1.setTextColor(Color.BLACK)
        btn_answer2.setTextColor(Color.BLACK)
        btn_answer3.setTextColor(Color.BLACK)
        btn_answer4.setTextColor(Color.BLACK)

    }


    /**
     * evaluate and send score to gameserver
     */
    fun sendScore() {
        var timeLeft = 21 - progress_question.progress / 1000
        timer.cancel()
        var timeLeft = (progress_timer.progress / 1000)

        if(currentQuestion.question is ChoiceQuestion){
            Client.send(MsgScore((currentQuestion.question as ChoiceQuestion).evaluate(choiceQuestionAnswer, timeLeft, 21)))
        } else {
            Client.send(MsgScore((currentQuestion.question as GuessQuestion).evaluate(guessQuestionAnswer, timeLeft, 21)))
        }
    }

    /**
     * Timer defined time to answer
     * if the player didn't aswer in this time
     * then
     */
    private fun timer(millisInFuture: Long, countDownInterval: Long): CountDownTimer  {
        return object: CountDownTimer(millisInFuture, countDownInterval) {
            override fun onTick(millisUntilFinished: Long) {
                progress_timer.progress = millisInFuture.toInt() - millisUntilFinished.toInt()

            }

            override fun onFinish() {
                if(!isGameOver)
                sendScore()
             }
        }
    }

    private fun updateCountdownText(timeLeft: Long) = launch{
        val seconds: Int = (timeLeft / 1000).toInt()
        text_view_countdown.text = String.format(Locale.getDefault(), "%02d", seconds)
    }


    /**
     * display ranking after each question
     */
    private fun showRanking(msg : MsgRanking) {
        launch{

            //show ranking view
            question_outer_layout.visibility = View.GONE
            reconnection_outer_layout.visibility = View.GONE
            score_outer_layout.visibility = View.VISIBLE

            val rowNicknameScoreViews = listOf<Triple<TableRow, TextView, TextView>>(
                    Triple(table_row_first, nickname_view1, score_view1), Triple(table_row_second, nickname_view2, score_view2),
                    Triple(table_row_third, nickname_view3, score_view3), Triple(table_row_fourth, nickname_view4, score_view4),
                    Triple(table_row_fifth, nickname_view4, score_view4), Triple(table_row_sixth, nickname_view6, score_view6),
                    Triple(table_row_seventh, nickname_view7, score_view7), Triple(table_row_eight, nickname_view8, score_view8)
            )
            val iter = msg.totalScore.iterator()

            //bind scores with the view
            for((index, value) in iter.withIndex()) {
                showHide(rowNicknameScoreViews[index].first)
                rowNicknameScoreViews[index].second.text = value.key
                rowNicknameScoreViews[index].third.text = value.value.toString()

                if(value.key.equals(nickname)){
                    finalScore = value.value
                }
            }
        }

        //display ranking for 3 seconds
        Thread.sleep(3000)
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

    /**
     * Display a choiceQuestion
     */

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


        var answerList: MutableList<String> = mutableListOf()
        answerList.addAll(question.choices)
        answerList.shuffle()

        question_text_view.text = question.text
        btn_answer1.text = answerList[0]
        btn_answer2.text = answerList[1]
        btn_answer3.text = answerList[2]
        btn_answer4.text = answerList[3]


    }


    /**
     * display GuessQuestion
     */
    fun showGuessQuestion(question: GuessQuestion){

        guess_container.visibility = View.VISIBLE
        answer_container.visibility = View.GONE

        question_text_view.text = question.text
        tv_lowest.text = "" + question.lowest
        tv_highest.text = "" + question.highest

        seekbar.min = question.lowest
        seekbar.max = question.highest
        seekbar.progress = (question.highest + question.lowest)/2

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
                tv_answer.text = "$i"
                guessQuestionAnswer = i

            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                btn_ok.visibility = View.VISIBLE

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
            }
        })

    }


    /**
     * display dialog when a player has lost the connection during a game
     * This enable the user to vote to wait for that player or not
     * the game continues when the app becomes a new question
     */

    fun displayDisconnectedPoll(msg: MsgConnectionLost) = launch {
        val builder = AlertDialog.Builder(this@QuizActivity)
        val view = layoutInflater.inflate(R.layout.layout_disconnect_poll, null)
        builder.setView(view)

        builder.setPositiveButton("wait") { _, _ ->
            Client.send(MsgVote(waitForPlayer = true, name = msg.name))

        }

        builder.setNegativeButton("don't wait") { _, _ ->
            Client.send(MsgVote(waitForPlayer = false, name = msg.name))

        }
        var dialog: AlertDialog = builder.create()
        dialog.show()

        var handler = Handler()

        var run = Runnable {
            if (dialog.isShowing) {
                dialog.dismiss()
            }
        }

        handler.postDelayed(run, 10000)
    }


    /**
     * OnBackpressed
     * Player can not quit a game voluntary
     */
    override fun onBackPressed() {
        if(isGameOver){
            super.onBackPressed()
       }
        /* else{
            //ask for confirmation bevor leave
            var ad = AlertDialog.Builder(context)
            ad.setTitle("Warning")
            ad.setMessage("Are you sure you want to leave this game?")

            ad.setPositiveButton("yes") { _, _ ->
                context.finish()
                Client.reconnectToMaster()
            }

            ad.setNegativeButton("Cancel") { _, _ ->
                ad.setCancelable(true)
            }
            ad.show()
        }*/
    }


    /**
     * Terminate a Game
     */
    fun finishQuiz() {
        isGameOver = true

        //if timer runs, cancel
        timer.cancel()

        //update UI with animation
        score_titel_view.text = "GAME OVER"
        tv_your_score.visibility = View.VISIBLE
        tv_your_score.text = "Your score: $finalScore"
        val a = AnimationUtils.loadAnimation(this, R.anim.anim_game_over)
        a.reset()
        score_titel_view.clearAnimation()
        score_titel_view.startAnimation(a)

        //close socket and reconnect to master
        Client.reconnectToMaster()

        //delete game's info
        clearGameInfo()

    }


    /**
     * delete all game's info saved on the device
     */
    private fun clearGameInfo() {
        val preferences = this.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.clear()
        editor.commit()
    }


    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    fun displayWaitingForReconnection() = launch {
        question_outer_layout.visibility = View.GONE
        score_outer_layout.visibility = View.GONE
        reconnection_outer_layout.visibility = View.VISIBLE
    }


}
