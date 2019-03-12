package org.quizfight.server

//import java.util.*
import org.quizfight.common.Connection
import org.quizfight.common.SocketConnection
import org.quizfight.common.messages.Message
import org.quizfight.common.messages.MsgPlayerCount
import org.quizfight.common.messages.MsgQuestion
import org.quizfight.common.messages.MsgRanking
import org.quizfight.common.question.Category
import org.quizfight.common.question.ChoiceQuestion
import org.quizfight.common.question.Question

/**
 * Game Class. Manages connections to players, asks Questions and calculates the scores
 * @author Thomas Spanier
 */
class Game(val id: String, val gameName:String, val maxPlayer: Int, var questions: MutableList<Question<*>>) {
    private val MSG_PLAYER_COUNT = "MaxPlayerCount must be between 2 and 8!"
    private val MSG_GAME_FULL = "The Game is already full!"


    var questionIncome: Int = 0
    var players: MutableMap<String, Player> = mutableMapOf<String, Player>()

    var playerCount = 0

    var isOpen: Boolean = true

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
            serverLog("Maximale Spieleranzahl erreicht. Spiel Startet in 3 Sekunden")
            Thread.sleep(3000)
            isOpen = false
            serverLog("Die maximale Spieler-Anzahl ist erreicht. Das Spiel startet\n")
            this.broadcast(MsgQuestion(ChoiceQuestion("Wer hat an der Uhr gedreht?",
                    Category.HISTORY,
                    listOf("Rapha", "Aude", "Julian", "Relaxo"),
                    "Rapha"))) // getNextQuestion()
        }
    }

    /**
     * Send a Message to all Players in this game
     *
     */
    fun broadcast(msg: Message){
        serverLog("$msg wird an folgende IP's gesendet")
        //serverLog(players.values.forEach{ getIpAndPortFromConnection(it.connection as SocketConnection) })
        players.values.forEach{ it.connection.send(msg) }
    }


    fun removePlayer(id: String){
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
        players.values.forEach{
            it.connection.close()
        }
        players.clear()
    }

    override fun toString(): String {
        return "Game(id=$id, gameName='$gameName', maxPlayer=$maxPlayer, open=$isOpen)"
    }

    fun createRanking(): Map<String, Int> {
        val ranking = hashMapOf<String, Int>()

        for (player in players){
            ranking.put(player.key, player.value.score)
        }

        val rankingSorted = ranking.toList().sortedBy { (_, value) -> value}.toMap()

        return rankingSorted
    }

    fun proceed() {
        questionIncome++


        if(questionIncome < players.size)
            return

        questionIncome = 0

        if(questions.size == 0){
            broadcast(MsgRanking(createRanking()))
        }else{
            questions.removeAt(0)

            broadcast(MsgQuestion(ChoiceQuestion("Wer hat an der Uhr gedreht?",
                    Category.AROUND_THE_WORLD,
                    listOf<String>("Relaxo", "Florian", "Mario", "Aude"), "Relaxo")))//broadcast(getNextQuestion())
        }
    }
}