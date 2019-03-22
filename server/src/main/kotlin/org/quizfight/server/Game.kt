package org.quizfight.server

//import java.util.*
import org.quizfight.common.Connection
import org.quizfight.common.SocketConnection
import org.quizfight.common.messages.*
import org.quizfight.common.question.Question

/**
 * Game Class. Manages connections to players, asks Questions and calculates the scores.
 * @param id is an unique id of a game.
 * @param gameName is the name of a game (clients can see this).
 * @param questions is a list of questions. Games will use it for playing the quiz.
 * @param idOfGameCreator is the unique id of the creator of this game.
 * @param gameCreatorName is the name of the creator of this game.
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
     * @param name is the name of the new player.
     * @param conn is the connection of the new player.
     */
    fun addPlayer(name: String, connection: Connection){
        var ipAndPort = getIpAndPortFromConnection(connection as SocketConnection)

        val player = Player(name, this, connection, ipAndPort)
        addPlayer(player)
    }

    /**
     * Adds a player to the game.
     * Game will start if the max player count of this game is reached.
     * @param player is the player (as Player()-object)
     */
    private fun addPlayer(player: Player) {
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

    /**
     * Starts a game.
     */
    fun startGame(){
        maxPlayer = players.size
        isOpen = false
        broadcast(getNextQuestion())
        startTimerForReceiveAnswers()
    }

    /**
     * Starts the round timer for each of the questions.
     * After this timer is over, the game will check if a connection is lost or not.
     * If each of the players has answered, the timer is also canceled and the next
     * round will start.
     */
    private fun startTimerForReceiveAnswers() {
        Thread {
            time = 0
            while(time < receiveAnswersTimer){
                Thread.sleep(1000)
                time++

                serverLog("Timer: $time / $receiveAnswersTimer")
                serverLog("Anzahl Spieler, die geantwortet haben: "  + playersAnswered.size + "\n"
                        + "Players insgesamt: "          + players.size + "\n")
            }

            // This is only true, if only one player is still playing
            // AND if he lost connection (because timer ran to maximum)
            if(players.size == 1 && maxPlayer != 1){
                terminateGame()
            }
        }.start()

    }

    /**
     * Send a Message to all Players in this game.
     * @param message is the Message type
     * @see common.messages.Message.kt
     */
    private fun broadcast(msg: Message){
        serverLog("${msg.javaClass.simpleName} geht an folgende Connections")
        players.values.forEach { println(getIpAndPortFromConnection(it.connection as SocketConnection)) }

        var connID = ""
        players.values.forEach {
            try {
                it.connection.send(msg)
            } catch (ex: Exception) {
                serverLog("Beim Broadcasten wurde mind. eine fehlerhafte Verbindung entdeckt\n")
                connID = it.id
            }
        }

        if(connID != "") {
            playersAnswered.remove(connID)
        }
     }

    /**
     * Removes a player from the game. This can happen if it loses the connection while playing
     * or if it leaves the lobby for waiting for game start.
     * @param id is the unique id of a player
     * @param conn is the connection of the player
     */
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
     * Return a message object with the next question for the next round.
     * @return is the message with the question.
     */
    private fun getNextQuestion(): Message{
        val question = questions[0]
        questions.removeAt(0)
        return MsgQuestion(question, questionCount - questions.size)
    }


    /**
     * Ends all connections between the Server and the mobile devices and clears the players list.
     * A flag (TERMINATED) is set, to remove this game from the game server.
     */
    private fun terminateGame(){
        serverLog("Schliesse folgende Verbindungen und beende das Spiel")
        players.values.forEach { println(getIpAndPortFromConnection(it.connection as SocketConnection)) }
        players.values.forEach{ it.connection.close() }
        players.clear()
        questions.clear()
        TERMINATED = true
    }

    /**
     * To-String-Function of the game class
     * @return is a string with information about id, gameName, maxPlayer, isOpen-state
     */
    override fun toString(): String {
        return "Game(id=$id, gameName='$gameName', maxPlayer=$maxPlayer, open=$isOpen)"
    }

    /**
     * Creates a ranking for showing the scores fter a round.
     * @return is the ranking as map from playername to score
     */
    private fun createRanking(): Map<String, Int> {
        val ranking = hashMapOf<String, Int>()

        for (player in players){
            ranking.put(player.value.name, player.value.score)
        }

        val rankingSorted = ranking.toList().sortedByDescending { (_, value) -> value }.toMap()


        return rankingSorted
    }

    /**
     * Heart of the game routine.
     * Waits until each of the players has answered a question.
     * If a player has not answered, the timer will run to its highest value. If this happens,
     * a connection loose is detected. The voting will introduced.
     * @param playerID is the id of the player which answered
     */
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

    /**
     * Stops the round timer.
     */
    private fun stopTimer() {
        time = receiveAnswersTimer
        Thread.sleep(2000)
    }

    /**
     * Game is over. This function will send a MsgGameOver to each of the players.
     * After, the ranking is sent.
     * Game will be terminated.
     */
    private fun gameOver() {
        broadcast(MsgGameOver())
        broadcast(MsgRanking(createRanking()))
        terminateGame()
    }

    /**
     * Introduces the next round. If a player has lost its connection,
     * the voting procedure will be sent to the players. After evaluationg,
     * the game will pause for an arbitrary amount of seconds (or not!).
     * @param playerLost is the boolean which decides, if a voting will be introduced.
     */
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

    /**
     * Sends a MsgConnectionLost to each of the players left.
     * After, the functions waits for the answers (WAIT or NO WAIT).
     * If they decide for wait, game will paused for waiting for the lost player.
     * @param missedPlayer is the player-object of the missed player.
     */
    private fun startVoteIfPlayerLeft(missedPlayer: Player) {
        broadcast(MsgConnectionLost(missedPlayer.name))

        serverLog("Warte ${voting.timerSendingVotes / 1000} Sekunden auf Votings der Clients...\n")
        Thread.sleep(voting.timerSendingVotes)

        var waitOrNot = voting.evaluateVoting()
        continueGameOrWaitForRejoin(waitOrNot)
    }

    /**
     * If the players want to wait for a lost player, this method will pause the game for
     * a determined value of seconds.
     * @param waitOrNot is the boolean. Its value comes from the evaluation of the players votes.
     * If true, game will pause for a value of seconds. If false, game will proceed.
     */
    private fun continueGameOrWaitForRejoin(waitOrNot: Boolean) {
        if(waitOrNot) {
            serverLog("Die Spieler möchten warten\n")
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

    /**
     * If a connection loose is detected this method will found out which of the players is lost.
     * @return is the Player-object of the lost player.
     */
    private fun checkWhichPlayerLeft() : Player {
        var player = players.values.first { !playersAnswered.contains(it.id) }
        return player
    }

}