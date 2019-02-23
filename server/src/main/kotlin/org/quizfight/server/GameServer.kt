package org.quizfight.server

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.quizfight.common.Connection
import org.quizfight.common.SocketConnection
import org.quizfight.common.messages.*
import org.quizfight.common.question.Question
import java.net.ServerSocket

/**
 * Game Server Class. Manages several Games.
 * @author Thomas Spanier
 */
//open class GameServer(val mip:String, val ip:String){
open class GameServer(){

    val socket = ServerSocket(34567)
    private var gameIds: Int = 0
    var games= mutableListOf<Game>()

    /*val connection = SocketConnection(socket.accept(), mapOf(
            //TODO: implement all handlers for Masterserver communication
            MsgGetOpenGames::class to   { conn, msg -> getOpenGames(conn, msg as MsgGetOpenGames) },
            MsgJoinGame::class to       { conn, msg -> joinGame(conn, msg as MsgJoinGame) },
            MsgCreateGame::class to     { conn, msg -> receiveCreateGame(conn, msg as MsgCreateGame)}
    ))*/

    init {
        //start()
    }

    /**
     * Adds a new game to gamesList
     */
    fun addNewGame(gameName: String, maxPlayer: Int, questions: MutableList<Question<Any>>){
        addNewGame(Game(gameIds++, gameName, maxPlayer, questions))
    }

    fun addNewGame(game: Game){
        games.add(game)
    }

    /**
     * lists all open games running on this server
     * @return List of all games in GameData format
     */
    fun listOpenGames(): List<GameData> {
        val openGames: List<Game> = games.filter { it.open }
        val gameData: MutableList<GameData> = mutableListOf()
        openGames.forEach { gameData.add(gameToGameData(it))  }
        return gameData
    }

    /**
     * converts a game into gameData
     */
    private fun gameToGameData(g: Game): GameData {
        var players = listOf<String>()

        for(i in 0..g.players.size){
            players.plus(g.players[i]!!.name)
        }

        return GameData(g.id, g.gameName, g.maxPlayer, players)
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
        //TODO: Implement
        println("Game Server started")

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

    /**
     * Gets called, if MsgCreateGame incomes.
     * Creates a new game and adds it to the games list
     * Sends feedback to Client
     */
    private fun receiveCreateGame(conn: Connection, msgCreateGame: MsgCreateGame) {
        // TODO Game Data hat keine questions mehr, der auskommentierte code kann also kein Game erstellen

        val gameRequest = msgCreateGame.game
       // this.addNewGame(gameDataToGame(gd))
        //conn.send(MsgSendGameCreated(gd))   //App gets Feedback, that the game was successfully created

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
        games[msgJoinGame.gameId].addPlayer(msgJoinGame.nickName, conn)
    }


}
