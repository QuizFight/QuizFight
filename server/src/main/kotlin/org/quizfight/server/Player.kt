package org.quizfight.server

import org.quizfight.common.Connection
import org.quizfight.common.SocketConnection
import org.quizfight.common.messages.*

/**
 * Player Class. Manages the Connection between a mobile device and the Server.
 * Includes player's score
 * @author Thomas Spanier
 */
class Player(val name : String, val game: Game, oldConnection: Connection, val id: String) {

    var score: Int = 0
    val connection = oldConnection.withHandlers(mapOf(
            //TODO: Vote, Timeout, etc
            MsgStartGame::class to { conn, msg -> startGame(conn, msg as MsgStartGame) },
            MsgScore::class to { conn, msg -> receiveAnswer(conn, msg as MsgScore) },
            MsgLeave::class to {conn, msg -> leaveGame(conn, msg as MsgLeave)},
            MsgVote::class to { conn, msg -> receiveVoteOrNot(conn, msg as MsgVote)}
    ))

    private fun receiveVoteOrNot(conn: Connection, msgVote: MsgVote) {
        game.voting.takeVote(msgVote.waitForPlayer)
    }


    /**
     * Forces the game to send the first question
     */
    private fun startGame(conn: Connection, msgStartGame: MsgStartGame) {
        serverLog("Ein Spieler hat das Spiel gestartet. Die erste Question wird gesendet\n")
        game.startGame()
    }

    /**
     * Player leaves game
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
     * Calculates score, removes game's first question and forces the game to send the next question
     */
    private fun receiveAnswer(conn: Connection, msgSendAnswer: MsgScore) {
        serverLog("Antwort erhalten von ${getIpAndPortFromConnection(conn)} \n")
        game.playersAnswered.add(getIpAndPortFromConnection(conn))
        addToScore(msgSendAnswer.score)
        game.proceed(id)
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

