
package org.quizfight.server

import org.quizfight.common.Connection
import org.quizfight.common.GAME_SERVER_PORT
import org.quizfight.common.SocketConnection
import org.quizfight.common.messages.*
import java.net.ServerSocket

/**
 * MasterServer is responsible for:
 * - Receiving updates from game servers
 * - Keeps a list of connected game server
 * - Show all open games to clients which want to join a game
 * - Forwards clients to game servers if they create a game or join a game
 * @param port is the port of the master server
 */
class MasterServer(private val port : Int) {
    private var gameServers = mutableListOf<ServerData>()

    /**
     * Inizializes handlers for listening to requests of clients and game servers.
     */
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

    /**
     * Handler for receiving an update from a game server.
     * The method will update the gamelist of a master server.
     * @param conn is the connection of the game server
     * @param msgGameList is the current gamelist of open games from a game server.
     */
    private fun receiveGameServerUpdate(conn: Connection, msgGameList: MsgGameList) {
        val remoteIpPort = getIpAndPortFromConnection(conn as SocketConnection)
        val remoteIp     = remoteIpPort.split(":")[0]

        gameServers.find { gs -> gs.ip == remoteIp && gs.port == GAME_SERVER_PORT }!!.games = msgGameList.games

        serverLog("GameListe erhalten von: $remoteIp:$GAME_SERVER_PORT")
        serverLog("Meine aktuelle Liste aller Spiele sieht so aus:\n" + listAllOpenGames() + "\n")
    }

    /**
     * Checks if a gameServer already is known.
     * @param gameServer is the server data of a game server
     * @return true, if server is known, false else
     */
    private fun gameServerIsKnown(gameServer: ServerData) : Boolean {
        val knownGameServer = gameServers.find {gs -> gs.ip == gameServer.ip && gs.port == gameServer.port}
        return knownGameServer != null
    }

    /**
     * Adds a game server to gameServers.
     * @param gameServer is the server data of a game server.
     */
    private fun addServerToList(gameServer: ServerData){
        gameServers.add(gameServer)
    }

    /**
     * Removes a game server from the list.
     * @param gameServer is the server data of the game server. It will be deleted from the list.
     */
    private fun removeServerFromList(gameServer: ServerData){
        gameServers.remove(gameServer)
    }

    /**
     * List all open games for clients.
     * @return is the list of open games.
     */
    private fun listAllOpenGames(): List<GameData>{
        return gameServers.map {gameServer -> gameServer.games}.flatten()
    }

    /**
     * Returns the game server with the lowest amount of games.
     * @return is the least used game server.
     */
    private fun getLeastUsedGameServer() : ServerData {
        gameServers.sortBy { it.games.size }
        return gameServers[0]
    }

    /**
     * Returns the server that hosts the game with param gameId.
     * @param gameId is the game id to look for.
     * @return is the optional server data.
     */
    private fun getServerByGameId(gameId : String) : ServerData? {
        return gameServers.find{ gameServer -> gameServer.hasGameWithId(gameId)}
    }

    /**
     * Extension method for ServerData. This way we don't have to get the actual game server.
     * @param gameId is the gameId to look for.
     * @return is a boolean, true if server has this game, false if not.
     */
    private fun ServerData.hasGameWithId(gameId: String) : Boolean {
        return games.find { it.id == gameId} != null
    }


    /**
     * Handler function for Request all games messages.
     * Wraps a list of all open games into a message object.
     * @param conn is the client asking for the open games
     */
    private fun sendAllGames(conn : Connection) {
        val games = listAllOpenGames()
        serverLog("Ich sende dem Client diese offenen Games:\n" + games + "\n")
        conn.send(MsgGameList(games))
    }

    /**
     * Handler method for Join Game requests.
     * Sends the server that manages the game in question.
     * @param conn is the connection from client
     * @param msgJoinGame is the message MsgJoin
     */
    private fun transferToGameServer(conn : Connection, msgJoinGame: MsgJoin) {
        val gameServer  = getServerByGameId(msgJoinGame.gameId)

        if (gameServer != null) {
            conn.send(MsgTransferToGameServer(gameServer))
            conn.close()
            serverLog("Spieler konnte dem Spiel nicht joinen\n")
            return
        }

        serverLog("Spieler ${msgJoinGame.nickname} möchte dem Spiel ${msgJoinGame.gameId} joinen")
        serverLog("Er wurde zu diesem GameServer vermittelt: ${gameServer}\n")
    }

    /**
     * Handler function for Create Game requests.
     * Sends the server with the lowest amount of games.
     * @param conn is the connection from the client
     * @param msgCreateGame is the MsgCreateGame from client.
     */
    private fun sendLeastUsedGameServer(conn : Connection, msgCreateGame: MsgCreateGame) {
        serverLog("Client möchte Spiel erstellen: ${msgCreateGame.game.name}")
        val gameServer = getLeastUsedGameServer()
        serverLog("Er erhält diesen Server dafür: ${gameServer} \n")
        conn.send(MsgTransferToGameServer(gameServer))
        conn.close()
    }


    /**
     * Handler function for Register game server requests.
     * Accepts a GameServerData object and updates its gameServers list accordingly.
     * @param conn is the connection of a game server,
     * @param msg is the message MsgRegisterGameServer holding information about the game server
     */
    private fun registerGameServer(conn: Connection, msg : MsgRegisterGameServer) {
        val remoteIpPort = getIpAndPortFromConnection(conn as SocketConnection)
        val remoteIp     = remoteIpPort.split(":")[0]
        val remotePort   = msg.port

        val gameServer = ServerData(remoteIp, remotePort, listOf<GameData>())

        if (gameServerIsKnown(gameServer)) {
            removeServerFromList(gameServer)
        }

        addServerToList(gameServer)
        serverLog("GameServer registriert! Seine Adresse: ${remoteIp}:${remotePort}")
        serverLog("Meine GameServer-Liste ist jetzt: \n" + gameServers + "\n")
    }
}