package org.quizfight.server

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.quizfight.common.Connection
import org.quizfight.common.GAME_SERVER_PORT
import org.quizfight.common.SocketConnection
import org.quizfight.common.messages.*
import org.quizfight.questionStore.QuestionStore
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException

/**
 * Game Server Class. Manages several Games and connects to the master server.
 * @param masterIp is the IP of the master server
 * @param ownPort is the port of this server (can be chosen freely)
 * @param masterPort is the port of the master
 */
open class GameServer(val masterIp: String, val ownPort: Int, val masterPort: Int){
    val questionStore = QuestionStore()    // hat ab diesem Zeitpunkt schon ALLE Fragen der XML-Dateien

    val masterConn = SocketConnection(Socket(masterIp, masterPort), mapOf())

    val socket = ServerSocket(ownPort)
    var games= mutableListOf<Game>()

    val UPDATE_INTERVALL = 2000L

    /**
     * After calling a GameServer()-constructor the server will...
     * ... connect to master
     * ... send periodical updates of the own gamelist
     * ... initialize connection handlers for receiving messages
     */
    init {
        connectWithMaster()
        sendUpdate()
        start()
    }

    /**
     * Peridoically sending the current gamelist to master server within a coroutine.
     */
    private fun sendUpdate(){
        GlobalScope.launch {
            while(true) {
                deleteTerminatedGames()
                masterConn.send(MsgGameList(gameListToGameDataList()))
                //serverLog("Sende diese GameList zum Master: " + games + "\n")
                delay(UPDATE_INTERVALL)
            }
        }
    }

    /**
     * Deletes terminated games from the gamelist.
     */
    private fun deleteTerminatedGames() {
        try{
            games.forEach { if (it.TERMINATED) { games.remove(it) }}
        }catch(ex: ConcurrentModificationException){
            //TODO Not the best way for deleting objects from this list but currently works
        }


    }


    /**
     * Lists all open games running on this server
     * @return List of all games in GameData format.
     */
    private fun listOpenGames(): List<GameData> {
        val openGames: List<Game> = games.filter { it.isOpen }
        val gameData: MutableList<GameData> = mutableListOf()
        openGames.forEach { gameData.add(gameToGameData(it))  }
        return gameData
    }

    /**
     * Converts a game into a gameData object. These objects are used by clients for showing
     * game information.
     * @param g is the Game which is going to be transformed to GameData
     */
    private fun gameToGameData(g: Game): GameData {
        var players = mutableListOf<String>()

        for(player in g.players){
            players.add(player.value.name)
        }

        return GameData(g.id, g.gameName, g.maxPlayer, players, g.questions.size, g.gameCreatorName)
    }

    /**
     * Takes the whole game list and transforms it to a gameData-list
     * @return is the gameData-list
     */
    private fun gameListToGameDataList(): List<GameData>{
        var gameDataList = mutableListOf<GameData>()

        for(game in games){
            if(game.isOpen && game.players.size > 0 && game.players.size < 8)
                gameDataList.add(gameToGameData(game))
        }

        return gameDataList
    }


    /**
     * Starts listening. The connections gets its handlers for handling messages from clients.
     */
    private fun start(){
        while (!socket.isClosed) {
            val incoming = socket.accept()
            GlobalScope.launch {
                SocketConnection(incoming, mapOf(
                        MsgGameList::class to { conn, msg -> getOpenGames(conn, msg as MsgGameList) },
                        MsgJoin::class to { conn, msg -> joinGame(conn, msg as MsgJoin) },
                        MsgCreateGame::class to { conn, msg -> receiveCreateGame(conn, msg as MsgCreateGame) },
                        MsgRejoin::class to { conn, msg -> rejoinGame(conn, msg as MsgRejoin) }
                ))
            }
        }
    }

    /**
     * After a player looses its connection, it may try to rejoin the game.
     * This function is the handler for this procedure.
     * @param conn is the connection of the rejoining player.
     * @param msgRejoin is the rejoin-message
     */
    private fun rejoinGame(conn: Connection, msgRejoin: MsgRejoin) {
        serverLog("Spieler ${msgRejoin.nickname} möchte rejoinen")

        val game = games.find { it.id == msgRejoin.gameServerID }

        if(game != null) {
            game.addPlayer(msgRejoin.nickname, conn)
        } else {
            serverLog("Sein altes Spiel wurde nicht mehr gefunden\n")
            conn.send(MsgGameOver())
            conn.close()
        }
    }

    /**
     * Connects with the master server.
     */
    private fun connectWithMaster() {
        try {
            masterConn.send(MsgRegisterGameServer(GAME_SERVER_PORT))
            serverLog("Mit Master verbunden\n")
        }catch(socEx: SocketException){
            println("Failed while connecting to Master")
        }
    }

    /**
     * Gets called, if MsgCreateGame is coming in.
     * Creates a new game and adds it to the games list
     * After,it send feedback to Client.
     * @param conn is the connection of the client.
     * @param msg is the MsgCreateGame coming from client.
     */

    private fun receiveCreateGame(conn: Connection, msg: MsgCreateGame) {
        val id = getIpAndPortFromConnection(conn as SocketConnection)

        val gameName = msg.game.name
        val maxPlayers = msg.game.maxPlayers
        val questionCount = msg.game.questionCount
        val gameCreator = msg.nickname

        val gameQuestions = questionStore.getQuestionsForGame(questionCount).toMutableList()

        val game = Game(id, gameName, maxPlayers, gameQuestions, id, gameCreator)
        game.addPlayer(gameCreator, conn)

        addNewGame(game)

        val gameData = GameData(id, gameName, maxPlayers, listOf<String>(gameCreator), questionCount, gameCreator)

        serverLog("Game erstellt: ${msg.game}")
        serverLog("Der Game-Creator ist ${gameCreator} und dieser wurde dem Game hinzugefügt\n")
        conn.send(MsgGameInfo(gameData))
    }

    /**
     * Adds a new game to gamesList
     * @param game is the new Game
     */
    private fun addNewGame(game: Game){
        games.add(game)
    }


    /**
     *  Gets called, if MsgGetOpenGames Message incomes.
     *  The handler sends the whole list of currently open games the client could join.
     *  @param conn is the connection to the client.
     *  @param msgGetOpenGames is the list of open games.
     */
    private fun getOpenGames(conn: Connection, msgGetOpenGames: MsgGameList) {
        conn.send(MsgGameList(listOpenGames()))
    }


    /**
     * Handler for MsgJoinGame.
     * If a player wants to join an open game, this method will add him to this game.
     * @param conn is the connection of the player.
     * @param msgJoinGame is the message MsgJoinGame
     */
    private fun joinGame(conn: Connection, msgJoinGame: MsgJoin) {
        serverLog("Join Game Anfrage von ${msgJoinGame.nickname}")

        val game = games.find { game -> game.id == msgJoinGame.gameId }
        if (game == null){
            serverLog("Game nicht gefunden! Client erhält eine MsgGameOver")
            conn.send(MsgGameOver())
            return
        }
        game!!.addPlayer(msgJoinGame.nickname, conn)
    }
}
