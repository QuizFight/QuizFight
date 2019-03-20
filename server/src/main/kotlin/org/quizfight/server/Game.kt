package org.quizfight.server

//import java.util.*
import org.quizfight.common.Connection
import org.quizfight.common.SocketConnection
import org.quizfight.common.messages.*
import org.quizfight.common.question.Question

/**
 * Game Class. Manages connections to players, asks Questions and calculates the scores
 */
class Game(val id: String, val gameName:String,
           var maxPlayer: Int,
           var questions: MutableList<Question<*>>,
           val idOfGameCreator: String,
           val gameCreatorName: String) {

    var players: MutableMap<String, Player> = mutableMapOf<String, Player>()

    var voting = Voting(this)

    var isOpen: Boolean = true
    var TERMINATED = false

    var playersAnswered = mutableListOf<String>()
    var receiveAnswersTimer = 25
    var time =  0

    val questionCount = questions.size

    var secondFilter = mutableListOf<String>()


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
        players.put(player.id, player)

        if(players.size > 1 && isOpen == true) {
            broadcast(MsgPlayerCount(players.size))
            serverLog("Der PlayerCount wurde an alle Spieler versendet, aktuell sind ${players.size} im Spiel\n")
        }

        if(players.size == maxPlayer && isOpen == true) {
            serverLog("Maximale Spieleranzahl erreicht. Spiel Startet in 2 Sekunden")
            Thread.sleep(2000)
            startGame()
        }
    }

    fun startGame(){
        maxPlayer = players.size
        isOpen = false
        broadcast(getNextQuestion())
        startTimerForReceiveAnswers()
    }

    fun startTimerForReceiveAnswers() {
        Thread {
            time = 0
            while(time < receiveAnswersTimer){
                Thread.sleep(1000)
                time++

                serverLog("Timer: $time / $receiveAnswersTimer")
                serverLog("playersAnswered: "  + playersAnswered.size + "\n"
                        + "players insgesamt: "          + players.size + "\n")
            }

            // This is only true, if only one player is still playing
            // AND if he lost connection (because timer ran to maximum)
            if(players.size == 1 && maxPlayer != 1){
                terminateGame()
            }
        }.start()

    }

    /**
     * Send a Message to all Players in this game
     */
    fun broadcast(msg: Message){
        serverLog("${msg.javaClass.simpleName} geht an folgende Connections")
        players.values.forEach { println(getIpAndPortFromConnection(it.connection as SocketConnection)) }
        players.values.forEach{ it.connection.send(msg) }
    }


    fun removePlayer(id: String, conn: Connection){
        conn.close()
        players.remove(id)

        if (id == idOfGameCreator && isOpen) {
            serverLog("Es war der Game Creator. Alle bekommen eine MsgGameOver\n")
            broadcast(MsgGameOver())
            terminateGame()
            return
        }

        broadcast(MsgPlayerCount(players.size))
    }


    /**
     * prepares the next question to be sended
     */
    fun getNextQuestion(): Message{
        val question = questions[0]
        questions.removeAt(0)
        return MsgQuestion(question, questionCount - questions.size)
    }


    /**
     * Ends all connections between the Server and the mobile devices and clears the players list
     */
    fun terminateGame(){
        serverLog("Schliesse folgende Verbindungen und beende das Spiel")
        players.values.forEach { println(getIpAndPortFromConnection(it.connection as SocketConnection)) }
        players.values.forEach{ it.connection.close() }
        players.clear()
        questions.clear()
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

        val rankingSorted = ranking.toList().sortedByDescending { (_, value) -> value }.toMap()


        return rankingSorted
    }


    fun proceed(playerID: String) {
        // Max. 2 players can pass this check, at least 1 player
        if((playersAnswered.size < players.size - 1)){
            return
        }

        secondFilter.add(playerID)

        var playerLost = waitForTimerOrAnswers()

        stopTimer()

        // Only ONE player may pass! So remove the second, if 2 players reached this point
        if(secondFilter.size == 2 && secondFilter[0] == playerID){
            return
        }
        secondFilter.clear()

        if(questions.size > 0){
            Thread{ nextRound(playerLost) }.start()
        }else{
            gameOver()
        }
    }

    private fun stopTimer() {
        time = receiveAnswersTimer
        Thread.sleep(2000)
    }

    private fun gameOver() {
        broadcast(MsgGameOver())
        broadcast(MsgRanking(createRanking()))
        terminateGame()
    }

    private fun nextRound(playerLost: Boolean) {
        if(playerLost){
            var missedPlayer = checkWhichPlayerLeft()
            serverLog(missedPlayer.name + " hat sich verabschiedet")
            missedPlayer.connection.close()
            players.remove(missedPlayer.id)
            broadcast(MsgRanking(createRanking()))
            startVoteIfPlayerLeft(missedPlayer)
        }else{
            broadcast(MsgRanking(createRanking()))
        }
        Thread.sleep(4000)
        playersAnswered = mutableListOf<String>()
        broadcast(getNextQuestion())
        startTimerForReceiveAnswers()
    }


    /**
     * Checks every second, if all answers came in
     * @return true, if player is lost, false all players answered.
     */
    private fun waitForTimerOrAnswers() : Boolean {
        while (time < receiveAnswersTimer) {
            Thread.sleep(1000)
            if (playersAnswered.size == players.size) {
                time = receiveAnswersTimer
                return false
            }
        }
        return true
    }

    private fun startVoteIfPlayerLeft(missedPlayer: Player) {
        broadcast(MsgConnectionLost(missedPlayer.name))

        serverLog("Warte ${voting.timerSendingVotes / 1000} Sekunden auf Votings der Clients...\n")
        Thread.sleep(voting.timerSendingVotes)

        var waitOrNot = voting.evaluateVoting()
        continueGameOrWaitForRejoin(waitOrNot)
    }

    private fun continueGameOrWaitForRejoin(waitOrNot: Boolean) {
        if(waitOrNot) {
            serverLog("Game $gameName wartet ${voting.votingWaitingTime / 1000} Sekunden auf den Spieler")
            broadcast(MsgWait())

            var currentTime = 0
            while(currentTime < voting.votingWaitingTime){
                serverLog("Warte noch ${voting.votingWaitingTime - currentTime} Sekunden auf den Spieler")
                Thread.sleep(1000)
                currentTime++
                if(players.size == maxPlayer){
                    break
                }
            }
        }else{
            serverLog("Die Spieler möchten nicht auf den verlorenen Spieler warten")
        }
    }

    private fun checkWhichPlayerLeft() : Player {
        var player = players.values.first { !playersAnswered.contains(it.id) }
        return player
    }

}