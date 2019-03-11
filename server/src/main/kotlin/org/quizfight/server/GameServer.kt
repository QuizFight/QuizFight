package org.quizfight.server

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.quizfight.common.Connection
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

    val UPDATE_INTERVALL = 8000L

    init {
        connectWithMaster()
        //addHardcodedGameForTesting()  // TEMPORARY
        sendUpdate()
        start()
    }

    private fun addHardcodedGameForTesting(){
        games.add(Game("1", "TestGame 1",8,
                questionStore.getQuestionsForGame(5).toMutableList()))
        games.add(Game("2", "TestGame 2",5,
                questionStore.getQuestionsForGame(9).toMutableList()))
    }


    private fun sendUpdate(){
        GlobalScope.launch {
            while(true) {
                masterConn.send(MsgGameList(gameListToGameDataList()))
                println("Sent this GameList to Master: " + games + "\n")
                delay(UPDATE_INTERVALL)
            }
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

        return GameData(g.id, g.gameName, g.maxPlayer, players, g.questions.size)
    }

    private fun gameListToGameDataList(): List<GameData>{
        var gameDataList = mutableListOf<GameData>()

        for(game in games){
            gameDataList.add(gameToGameData(game))
        }

        return gameDataList
    }

    /**
     * converts gameData into a game
     */
   /* private fun gameDataToGame(gd: GameData): Game {
        // TODO: Game braucht Question list
        return Game(gd.id,gd.name, gd.maxPlayers)
    }*/


    /**
     * starts Slave Server
     */
    open fun start(){
        while (!socket.isClosed) {
            val incoming = socket.accept()
            GlobalScope.launch {
                SocketConnection(incoming, mapOf(
                        //TODO: implement all handlers for Masterserver communication
                        MsgGameList::class to { conn, msg -> getOpenGames(conn, msg as MsgGameList) },
                        MsgJoin::class to { conn, msg -> joinGame(conn, msg as MsgJoin) },
                        MsgCreateGame::class to { conn, msg -> receiveCreateGame(conn, msg as MsgCreateGame) }
                ))
                println("Client connected")
            }
        }
    }

    private fun connectWithMaster() {
        try {
            masterConn.send(MsgRegisterGameServer())
        }catch(socEx: SocketException){
            println("Failed while connecting to Master")
            return
        }

        println("Connected to Master")
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

        val game = Game(id, gameName, maxPlayers, gameQuestions)
        game.addPlayer(gameCreator, conn)

        addNewGame(game)

        val gameData = GameData(id, gameName, maxPlayers, listOf<String>(), questionCount)

        println("Game erstellt. Die ID ist: ${id}")
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
        val game = games.find { game -> game.id == msgJoinGame.gameId }
        if (game == null){
            return
        }
        game!!.addPlayer(msgJoinGame.nickname, conn)

        val gameData = gameToGameData(game!!)

        serverLog("Spieler ${msgJoinGame.nickname} möchte dem Spiel ${game.gameName} joinen")
    }


}
