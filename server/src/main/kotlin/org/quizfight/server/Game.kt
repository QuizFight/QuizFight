package org.quizfight.server

//import java.util.*
import org.quizfight.common.Connection
import org.quizfight.common.SocketConnection
import org.quizfight.common.messages.Message
import org.quizfight.common.messages.MsgPlayerCount
import org.quizfight.common.messages.MsgQuestion
import org.quizfight.common.messages.MsgRanking
import org.quizfight.common.question.Question

/**
 * Game Class. Manages connections to players, asks Questions and calculates the scores
 * @author Thomas Spanier
 */
class Game(val id: String, val gameName:String, val maxPlayer: Int,
           var questions: MutableList<Question<*>>, val idOfGameCreator: String) {
    private val MSG_PLAYER_COUNT = "MaxPlayerCount must be between 2 and 8!"
    private val MSG_GAME_FULL = "The Game is already full!"


    var answersIncome: Int = 0
    var players: MutableMap<String, Player> = mutableMapOf<String, Player>()


    var votes = mutableMapOf<Boolean, Int>((false to 0), (true to 0))
    var timerForWaiting = 30  // seconds
    var playerIsBack = false

    var playerCount = 0

    var isOpen: Boolean = true
    var TERMINATED = false


    fun takeVote(vote: Boolean){
        var count = votes.get(vote)!!
        votes.put(vote, count + 1)

        if(votes.get(false)!! + votes.get(true)!! == playerCount - 1){
            evaluateVoting()
        }
    }

    private fun evaluateVoting() {
        serverLog("Voting wird jetzt ausgewertet\n")
        if(votes.get(true)!! >= votes.get(false)!!){
            waitForPlayer()
        }

        broadcast(getNextQuestion())
    }

    private fun waitForPlayer() {
        while(timerForWaiting > 0){
            serverLog("Game $gameName wartet noch $timerForWaiting auf den Spieler")

            Thread.sleep(1000)
            timerForWaiting--

            if(playerIsBack){
                resetVotingLogic()
                break
            }
        }
    }

    private fun resetVotingLogic(){
        votes = mutableMapOf<Boolean, Int>((false to 0), (true to 0))
        playerIsBack = false
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
    fun addPlayer(name: String, connection: Connection){
        var ipAndPort = getIpAndPortFromConnection(connection as SocketConnection)

        val player = Player(name, this, connection, ipAndPort)
        addPlayer(player)
    }

    fun addPlayer(player: Player) {
        if(players.size >= maxPlayer) {
            serverLog("Player size >= maxPlayer")
            throw Exception(MSG_GAME_FULL)
            return
        }
        players.put(player.id, player)

        playerCount++
        if(playerCount > 1) {
            broadcast(MsgPlayerCount(playerCount))
            serverLog("Der PlayerCount wurde an alle Spieler versendet, aktuell sind ${playerCount} in der Lobby\n")
        }

        if(playerCount == maxPlayer) {
            isOpen = false
            serverLog("Maximale Spieleranzahl erreicht. Spiel Startet in 3 Sekunden")
            var question =
            Thread.sleep(3000)
            broadcast(getNextQuestion())
        }
    }

    /**
     * Send a Message to all Players in this game
     *
     */
    fun broadcast(msg: Message){
        players.values.forEach{ it.connection.send(msg) }
    }


    fun removePlayer(id: String, conn: Connection){
        if(id == idOfGameCreator) {
            terminateGame()
            return
        }
        conn.close()
        players.remove(id)
        broadcast(MsgPlayerCount(--playerCount))
    }


    /**
     * prepares the next question to be sended
     */
    fun getNextQuestion(): Message{
        val question = questions[0]
        questions.removeAt(0)
        return MsgQuestion(question)
    }

    fun printQuestions(){
        var i = 1;
        for(question in questions){
            println("Frage " + i + ": " + question.text)
            i++
        }
    }


    /**
     * Ends all connections between the Server and the mobile devices and clears the players list
     */
    fun terminateGame(){
        players.values.forEach{ it.connection.close() }
        players = mutableMapOf<String, Player>()
        TERMINATED = true
    }

    override fun toString(): String {
        return "Game(id=$id, gameName='$gameName', maxPlayer=$maxPlayer, open=$isOpen)"
    }

    fun createRanking(): Map<String, Int> {
        val ranking = hashMapOf<String, Int>()

        for (player in players){
            ranking.put(player.value.name, player.value.score)
        }

        val rankingSorted = ranking.toList().sortedBy { (_, value) -> value}.toMap()

        return rankingSorted
    }

    fun proceed() {
        answersIncome++

        if(answersIncome < players.size)
            return

        answersIncome = 0

        broadcast(MsgRanking(createRanking()))

        if(questions.size > 0){
            broadcast(getNextQuestion())
        }else{
            terminateGame()
        }
    }
}