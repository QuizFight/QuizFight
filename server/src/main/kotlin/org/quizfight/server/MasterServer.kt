package org.quizfight.server

import org.quizfight.common.Connection
import org.quizfight.common.SocketConnection
import org.quizfight.common.messages.*
import java.net.ServerSocket


/**
 * The master server that manages game servers.
 * First contact point for all clients and game servers.
 * @author Phillip Persch
 */
class MasterServer(private val port : Int) {
    private var gameServers = mutableListOf<GameServerData>()

    init{
        acceptConnections()
    }

    /**
     * Accepts all incoming connections.
     */
    private fun acceptConnections() {
        val socket = ServerSocket(port)
        while (!socket.isClosed) {
            val connection = SocketConnection(socket.accept(), mapOf(
                    MsgRegisterGameServer::class to { conn, msg -> registerGameServer(conn, msg as MsgRegisterGameServer) },
                    MsgRequestAllGames::class to { conn, _ -> sendAllGames(conn) },
                    MsgJoinGame::class to { conn, msg -> transferToGameServer(conn, msg as MsgJoinGame) },
                    MsgCreateGame::class to { conn, msg -> sendLeastUsedGameServer(conn, msg as MsgCreateGame)}
            ))
        }
    }


    /**
     * Checks if a gameServer already is known.
     */
    private fun gameServerIsKnown(gameServer: GameServerData) : Boolean {
        val knownGameServer = gameServers.find {gs -> gs.ip == gameServer.ip && gs.port == gameServer.port}
        return knownGameServer != null
    }

    /**
     * Adds a game server to gameServers.
     */
    private fun addServerToList(gameServer: GameServerData){
        gameServers.add(gameServer)
    }

    /**
     * Removes a game server from gameServers.
     */
    private fun removeServerFromList(gameServer: GameServerData){
        gameServers.remove(gameServer)
    }
    /**
     * returns a List with all open Games from all game servers.
     */
    private fun listAllOpenGames(): List<GameData>{
        return gameServers.map {gameServer -> gameServer.games}.flatten()
    }

    /**
     * Returns the game server with the lowest amount of games.
     */
    private fun getLeastUsedGameServer() : GameServerData {
        gameServers.sortBy { it.games.size}
        return gameServers.first()
    }

    /**
     * Returns the server that hosts the game with param gameId.
     */
    private fun getServerByGameId(gameId : Int) : GameServerData? {
        return gameServers.find{ gameServer -> gameServer.hasGameWithId(gameId)}
    }


    private fun GameServerData.hasGameWithId(gameId: Int) : Boolean {
        return games.find { it.id == gameId} != null
    }



    // HANDLERS

    /**
     * Wraps a list of all open games into a message object.
     */
    private fun sendAllGames(conn : Connection) {
        val games = listAllOpenGames()
        conn.send(MsgGameList(games))
    }

    /**
     * Handler method for Join Game requests.
     * Sends the server that manages the game in question.
     */
    private fun transferToGameServer(conn : Connection, msgJoinGame: MsgJoinGame) {

        val gameServer  = getServerByGameId(msgJoinGame.gameId)

        //TODO: What if == null?
        if (gameServer != null) {
            conn.send(MsgTransferToGameServer(gameServer))
        }
    }

    /**
     * Handler function for Create Game requests.
     * Sends the server with the lowest amount of games.
     */
    private fun sendLeastUsedGameServer(conn : Connection, msgJoinGame: MsgCreateGame) {
        val gameServer = getLeastUsedGameServer()
        conn.send(MsgTransferToGameServer(gameServer))
    }



    private fun registerGameServer(conn: Connection, msg : MsgRegisterGameServer) {

        val gameServer = msg.gameServer

        if (gameServerIsKnown(gameServer)) {
            removeServerFromList(gameServer)
        }
        addServerToList(gameServer)
    }
}

