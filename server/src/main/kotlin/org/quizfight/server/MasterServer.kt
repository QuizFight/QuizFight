
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
    private var gameServers = mutableListOf<ServerData>()
    private var gameDataList = mutableListOf<GameData>()

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
                    MsgRegisterGameServer::class to { conn, msg -> registerGameServer(conn, msg as MsgRegisterGameServer)},
                    MsgRequestOpenGames::class to { conn, _ -> sendAllGames(conn) },
                    MsgJoin::class to { conn, msg -> transferToGameServer(conn, msg as MsgJoin) },
                    MsgCreateGame::class to { conn, msg -> sendLeastUsedGameServer(conn, msg as MsgCreateGame)},
                    MsgGameList::class to { conn, msg -> receiveGameServerUpdate(conn, msg as MsgGameList)}
            ))
        }
    }

    private fun receiveGameServerUpdate(conn: Connection, msgGameList: MsgGameList) {
        val remoteIpPort = getIpAndPortFromConnection(conn as SocketConnection)
        val remoteIp     = remoteIpPort[0]
        val remotePort   = remoteIpPort[1].toInt()

        gameServers.find { gs -> gs.ip == remoteIp && gs.port == remotePort }!!.games = msgGameList.games

        serverLog("GameListe erhalten von: ${remoteIp}:${remotePort}")
        serverLog("Meine aktuelle Liste aller Spiele sieht so aus:\n" + listAllOpenGames() + "\n")
    }

    /**
     * Checks if a gameServer already is known.
     */
    private fun gameServerIsKnown(gameServer: ServerData) : Boolean {
        val knownGameServer = gameServers.find {gs -> gs.ip == gameServer.ip && gs.port == gameServer.port}
        return knownGameServer != null
    }

    /**
     * Adds a game server to gameServers.
     */
    private fun addServerToList(gameServer: ServerData){
        gameServers.add(gameServer)
    }

    /**
     * Removes a game server from gameServers.
     */
    private fun removeServerFromList(gameServer: ServerData){
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
    private fun getLeastUsedGameServer() : ServerData {
        gameServers.sortBy { it.games.size }
        return gameServers.first()
    }

    /**
     * Returns the server that hosts the game with param gameId.
     */
    private fun getServerByGameId(gameId : String) : ServerData? {
        return gameServers.find{ gameServer -> gameServer.hasGameWithId(gameId)}
    }

    /**
     * Extension method for ServerData. This way we don't have to get the actual game server.
     */
    private fun ServerData.hasGameWithId(gameId: String) : Boolean {
        return games.find { it.id == gameId} != null
    }



    // HANDLERS

    /**
     * Handler function for Request all games messages.
     * Wraps a list of all open games into a message object.
     */
    private fun sendAllGames(conn : Connection) {
        val games = listAllOpenGames()
        serverLog("Ich sende dem Client diese offenen Games:\n" + games + "\n")
        conn.send(MsgGameList(games))
    }

    /**
     * Handler method for Join Game requests.
     * Sends the server that manages the game in question.
     */
    private fun transferToGameServer(conn : Connection, msgJoinGame: MsgJoin) {
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


    /**
     * Handler function for Register game server requests.
     * Accepts a GameServerData object and updates its gameServers list accordingly.
     */
    private fun registerGameServer(conn: Connection, msg : MsgRegisterGameServer) {
        // TODO ip und port werden wahrscheinlich anders mitgegeben, rücksprache!
        val remoteIpPort = getIpAndPortFromConnection(conn as SocketConnection)
        val remoteIp     = remoteIpPort[0]
        val remotePort   = remoteIpPort[1].toInt()

        val gameServer = ServerData(remoteIp, remotePort, listOf<GameData>())

        if (gameServerIsKnown(gameServer)) {
            removeServerFromList(gameServer)
        }

        addServerToList(gameServer)
        serverLog("GameServer registriert! Seine Adresse: ${remoteIp}:${remotePort}")
        serverLog("Meine GameServer-Liste ist jetzt: \n" + gameServers + "\n")
    }
}