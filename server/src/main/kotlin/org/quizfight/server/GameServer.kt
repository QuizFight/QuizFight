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
 * Game Server Class. Manages several Games.
 * @author Thomas Spanier
 */
open class GameServer(val masterIp: String, val ownPort: Int, val masterPort: Int){
    val questionStore = QuestionStore()    // hat ab diesem Zeitpunkt schon ALLE Fragen der XML-Dateien

    val masterConn = SocketConnection(Socket(masterIp, masterPort), mapOf())

    val socket = ServerSocket(ownPort)
    var games= mutableListOf<Game>()

    val UPDATE_INTERVALL = 4000L

    init {
        connectWithMaster()
        sendUpdate()
        start()
    }


    private fun sendUpdate(){
        GlobalScope.launch {
            while(true) {
                deleteTerminatedGames()
                masterConn.send(MsgGameList(gameListToGameDataList()))
                serverLog("Sende diese GameList zum Master: " + games + "\n")
                delay(UPDATE_INTERVALL)
            }
        }
    }

    private fun deleteTerminatedGames() {
        try{
            games.forEach { if (it.TERMINATED) { games.remove(it) }}
        }catch(ex: ConcurrentModificationException){
            //TODO tritt auf, funktioniert aber.
        }


    }


    /**
     * lists all open games running on this server
     * @return List of all games in GameData format
     */
    fun listOpenGames(): List<GameData> {
        val openGames: List<Game> = games.filter { it.isOpen }
        val gameData: MutableList<GameData> = mutableListOf()
        openGames.forEach { gameData.add(gameToGameData(it))  }
        return gameData
    }

    /**
     * converts a game into gameData
     */
    private fun gameToGameData(g: Game): GameData {
        var players = mutableListOf<String>()

        for(player in g.players){
            players.add(player.value.name)
        }

        return GameData(g.id, g.gameName, g.maxPlayer, players, g.questions.size, g.gameCreatorName)
    }

    private fun gameListToGameDataList(): List<GameData>{
        var gameDataList = mutableListOf<GameData>()

        for(game in games){
            if(game.isOpen && game.players.size > 0 && game.players.size < 8)
                gameDataList.add(gameToGameData(game))
        }

        return gameDataList
    }


    /**
     * starts Slave Server
     */
    open fun start(){
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

    private fun rejoinGame(conn: Connection, msgRejoin: MsgRejoin) {
        serverLog("Spieler ${msgRejoin.nickname} möchte rejoinen")

        val game = games.find { it.id == msgRejoin.gameServerID }

        if(game != null) {
            game.addPlayer(msgRejoin.nickname, conn)
        } else {
            serverLog("Sein altes Spiel wurde nicht mehr gefunden\n")
            conn.send(MsgGameOver())
        }
    }

    private fun connectWithMaster() {
        try {
            masterConn.send(MsgRegisterGameServer(GAME_SERVER_PORT))
            serverLog("Mit Master verbunden\n")
        }catch(socEx: SocketException){
            println("Failed while connecting to Master")
        }
    }

    /**
     * Gets called, if MsgCreateGame incomes.
     * Creates a new game and adds it to the games list
     * Sends feedback to Client
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
     */
    fun addNewGame(game: Game){
        games.add(game)
    }


    /**
     *  Gets called, if MsgGetOpenGames Message incomes.
     *  Sends the list of open games over own connection object
     *  TODO: use conn or connection?
     */
    private fun getOpenGames(conn: Connection, msgGetOpenGames: MsgGameList) {
        conn.send(MsgGameList(listOpenGames()))
        //connection.send(MsgSendOpenGames(listOpenGames()))
    }


    /**
     * Gets called, if MsgJoinGame incomes.
     * Adds the Player to a Game
     */
    private fun joinGame(conn: Connection, msgJoinGame: MsgJoin) {
        serverLog("Join Game Anfrage von ${msgJoinGame.nickname}")

        val game = games.find { game -> game.id == msgJoinGame.gameId }
        if (game == null){
            serverLog("Game ist null!")
            return
        }
        game!!.addPlayer(msgJoinGame.nickname, conn)
    }

    fun removeGame(id: String) {
        for(game in games){
            if(game.id == id){
                games.remove(game)
                serverLog("Spiel ${game.gameName} entfernt\n")
                return
            }
        }
    }


}
