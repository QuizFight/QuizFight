package org.quizfight.server

//import java.util.*
import org.quizfight.common.Connection
import org.quizfight.common.SocketConnection
import org.quizfight.common.messages.*
import org.quizfight.common.question.Question
import java.io.IOException

/**
 * Game Class. Manages connections to players, asks Questions and calculates the scores
 * @author Thomas Spanier
 */
class Game(val id: String, val gameName:String, val maxPlayer: Int,
           var questions: MutableList<Question<*>>, val idOfGameCreator: String) {

    private val MSG_GAME_FULL = "The Game is already full!"


    var answersIncome: Int = 0
    var players: MutableMap<String, Player> = mutableMapOf<String, Player>()


    var voting = Voting(this)

    var playerCount = 0

    var isOpen: Boolean = true
    var TERMINATED = false


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

        if(questions.size > 0){
            broadcast(MsgRanking(createRanking()))
            broadcast(getNextQuestion())
        }else{
            broadcast(MsgGameOver())
            broadcast(MsgRanking(createRanking()))
            terminateGame()
        }
    }

    private fun checkConnections() {
        players.forEach {
            try {
                it.value.connection.send(MsgCheckConnection())
            }catch(ex: IOException){
                serverLog("Client ${it.value.name} hat die Verbindung verloren. Voting wird gesendet\n")
                sendVoting(it.value.id)
                return
            }
        }
    }

    /**
     * Sends a voting message to all clients still connected
     * @param id is the missed client
     */
    private fun sendVoting(id: String) {
        var nickname = ""

        players.forEach{
            if(id == it.value.id) {
                players.remove(id)
                nickname = it.value.name
            }
        }

        serverLog("Sende Voting zu allen verbliebenen Clients")
        broadcast(MsgConnectionLost(nickname))

    }
}