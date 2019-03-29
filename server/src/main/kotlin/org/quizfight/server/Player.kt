package org.quizfight.server

import org.quizfight.common.Connection
import org.quizfight.common.SocketConnection
import org.quizfight.common.messages.MsgLeave
import org.quizfight.common.messages.MsgScore
import org.quizfight.common.messages.MsgStartGame
import org.quizfight.common.messages.MsgVote
import java.io.EOFException

/**
 * Player Class. Manages the Connection between a mobile device and the Server.
 * @param name is the name of a player
 * @param game is the game which the player joines
 * @param oldConnection is the players connection from a point he was not part of a game.
 * This connection carries old handlers, which will be updated in this class.
 * @param id is an unique id of a player.
 */
class Player(val name : String, val game: Game, oldConnection: Connection, val id: String) {

    var score: Int = 0
    lateinit var connection: Connection

    /**
     * Initializes new handlers for a player joined a game.
     */
    init{
        try {
            connection = oldConnection.withHandlers(mapOf(
                    MsgStartGame::class to { conn, msg -> startGame(conn, msg as MsgStartGame) },
                    MsgScore::class to { conn, msg -> receiveAnswer(conn, msg as MsgScore) },
                    MsgLeave::class to { conn, msg -> leaveGame(conn, msg as MsgLeave) },
                    MsgVote::class to { conn, msg -> receiveVoteOrNot(conn, msg as MsgVote) }))
        }catch(ex: EOFException){

        }
    }

    /**
     * If a player wants to wait or wants not to wait for another lost player,
     * this method will receive the decision.
     * @param conn is the connection of the player.
     * @param msgVote is the Message if it wants to wait or not.
     */
    private fun receiveVoteOrNot(conn: Connection, msgVote: MsgVote) {
        game.voting.takeVote(msgVote.waitForPlayer)
    }


    /**
     * Forces the game to send the first question.
     * @param conn is the connection of the player which forced the start.
     * @param msgStartGame is the MsgStartGame-object.
     */
    private fun startGame(conn: Connection, msgStartGame: MsgStartGame) {
        serverLog("Ein Spieler hat das Spiel gestartet. Die erste Question wird gesendet\n")
        game.startGame()
    }

    /**
     * If a player leaves a game it will send a MsgLeave-message.
     * After, it will be kicked from the waiting room after joining a game (which has not started yet!)
     */
    private fun leaveGame(conn: Connection, msgLeave: MsgLeave) {
        serverLog("Spieler verlässt das Game")

        val ipAndPort = getIpAndPortFromConnection(conn as SocketConnection)

        for(player in game.players){
            if(player.key == ipAndPort){
                game.removePlayer(id, conn)
                return
            }
        }
    }

    /**
     * Handler for receiving the score of a player.
     * Further, this method will trigger the proceed-method of the running game.
     * @param conn is the connection to the player.
     * @param msgSendAnswer is the score for an answered question.
     */
    private fun receiveAnswer(conn: Connection, msgSendAnswer: MsgScore) {
        serverLog("Antwort erhalten von ${getIpAndPortFromConnection(conn)} \n")
        game.playersAnswered.add(getIpAndPortFromConnection(conn))
        addToScore(msgSendAnswer.score)
        game.proceed(id)
    }


    /**
     * Adds value to the player's score
     * @param value is the value to add.
     */
    private fun addToScore(value:Int){
        score += value
    }


    /**
     * Returns information about a player.
     * @return is a string with information about player name and its score.
     */
    override fun toString(): String {
        return "Player(name='$name', score=$score)"
    }


}

