package org.quizfight.server

//import java.util.*
import org.quizfight.common.question.Question

/**
 * Game Class. Manages connections to players, asks Questions and calculates the scores
 * @author Thomas Spanier
 */
class Game(id: Int, gameName:String, maxPlayer: Int, questionCount: Int) {
    private val MSG_PLAYER_COUNT = "MaxPlayerCount must be between 2 and 8!"
    private val MSG_GAME_FULL = "The Game is already full!"
    private val id: Int
    private val gameName: String
    private val maxPlayer: Int
    private val questionCount: Int

    private var questions: MutableList<Question>
    private var players: MutableList<Player>
    private var open: Boolean

    /**
     * Constructor. Sets ID, GameName, max number of players and questions
     */
    init {
        this.id = id
        this.gameName = gameName
        this.maxPlayer = setMaxPlayer(maxPlayer)
        this.questionCount = questionCount
        open = true
        //players = MutableList(0){Player("", "")}
        players = arrayListOf()
        questions = arrayListOf()                                               //TODO: add Questions
        //questions = MutableList(questionCount) { TmpQuestion(i++) }
        //questions.forEach{x -> println(x.returnA())}
    }

    fun getOpen() : Boolean {
        return open
    }

    fun setOpen(open: Boolean) {
        this.open = open
    }

    /**
     * Sets the max number of players. Must be between 2 and 8
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
    fun addPlayer(name: String){
        if(players.size >= maxPlayer) {
            throw Exception(MSG_GAME_FULL)
        }
        //val player = Player(name, ip)
        players.add(Player(name))
    }

    /**
     * Ends all connections between the Server and the mobile devices and clears the players list
     */
    fun endGame(){
        //TODO: End all connections
        players.clear()
    }

    override fun toString(): String {
        return "Game(id=$id, gameName='$gameName', maxPlayer=$maxPlayer, questionCount=$questionCount, open=$open)"
    }


}