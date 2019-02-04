package org.quizfight.server

//import java.util.*
import org.quizfight.common.messages.Message
import org.quizfight.common.messages.MsgSendQuestion
import org.quizfight.common.question.Question
import java.net.Socket

/**
 * Game Class. Manages connections to players, asks Questions and calculates the scores
 * @author Thomas Spanier
 */
class Game(val id: Int, val gameName:String, val maxPlayer: Int, val questionCount: Int) {
    private val MSG_PLAYER_COUNT = "MaxPlayerCount must be between 2 and 8!"
    private val MSG_GAME_FULL = "The Game is already full!"

    private var nextPlayerID: Int = 0
    var questions: MutableList<Question>
    var players: MutableMap<Int, Player> = mutableMapOf<Int, Player>()
    var open: Boolean = true

    /**
     * Constructor. Gets the questions
     */
    init {
        questions = mutableListOf<Question>()      //loadQuestions()
    }
    /**
     * Sets the max number of players. Must be between 2 and 8
     * TODO: Reimplement
     */
    private fun setMaxPlayer(maxPlayer: Int):Int{
        if (maxPlayer>8 || maxPlayer < 2) {
            throw java.lang.IllegalArgumentException(MSG_PLAYER_COUNT)
        } else {
            return maxPlayer
        }
    }

    /**
     * Adds a player to the game
     *
     */
    fun addPlayer(name: String, socket: Socket){
        val player = Player(name, this, socket)
        addPlayer(player)
    }

    fun addPlayer(player: Player) {
        if(players.size >= maxPlayer) {
            throw Exception(MSG_GAME_FULL)
        }
        players[nextPlayerID++] = player
        //TODO: If Game full, then start first question
        this.broadcast(getNextQuestion())
    }

    /**
     * Send a Message to all Players in this game
     *
     */
    fun broadcast(msg: Message){
        players.values.forEach{ it.connection.send(msg) }
    }


    /**
     * gets questions from questionSelector and stores them into questions
     */
    private fun loadQuestions(): MutableList<Question>{
        var qs: MutableList<Question> = mutableListOf<Question>()
        return qs
    }


    /**
     * prepares the next question to be sended
     */
    fun getNextQuestion(): Message{
        return MsgSendQuestion(questions[0])
    }



    /**
     * Ends all connections between the Server and the mobile devices and clears the players list
     */
    fun terminateGame(){
        players.values.forEach{
            //it.connection.socket.close()
            //it.socket.close()                   //TODO: Useless?
        }
        players.clear()
    }

    override fun toString(): String {
        return "Game(id=$id, gameName='$gameName', maxPlayer=$maxPlayer, open=$open)"
    }


}