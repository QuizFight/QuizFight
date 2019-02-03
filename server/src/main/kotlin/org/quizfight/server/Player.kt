package org.quizfight.server

import org.quizfight.common.Connection
import org.quizfight.common.messages.MsgSendAnswer
import org.quizfight.common.messages.MsgStartGame
import java.net.Socket

/**
 * Player Class. Manages the Connection between a mobile device and the Server.
 * Includes player's score
 * @author Thomas Spanier
 */
class Player(val name : String, val game: Game, val socket: Socket) {
    //private val name: String
    var score: Int = 0
    val connection = Connection(socket, mapOf(
            //TODO: Vote, Timeout, etc
            MsgStartGame::class to { conn, msg -> startGame(conn, msg as MsgStartGame) },
            MsgSendAnswer::class to { conn, msg -> receiveAnswer(conn, msg as MsgSendAnswer) }
    ))


    /**
     * Forces the game to send the first question
     */
    private fun startGame(conn: Connection, msgStartGame: MsgStartGame) {
        game.broadcast(game.getNextQuestion())
    }

    /**
     * Calculates score, removes game's first question and forces the game to send the next question
     */
    private fun receiveAnswer(conn: Connection, msgSendAnswer: MsgSendAnswer) {
        addToScore(msgSendAnswer.score)

        //TODO: temporary for first prototype
        game.questions.removeAt(0)
        game.broadcast(game.getNextQuestion())
    }


    /**
     * Adds value to the player's score
     * @param value
     */
    fun addToScore(value:Int){
        score += value
    }


    /**
     * returns true, if player name and game name are equal
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Player

        if (name != other.name) return false
        if(game.gameName != other.game.gameName) return false

        return true
    }

    override fun toString(): String {
        return "Player(name='$name', score=$score)"
    }


}
