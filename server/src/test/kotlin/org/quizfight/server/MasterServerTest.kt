package org.quizfight.server

import javafx.application.Application.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.*
import org.quizfight.common.Connection
import org.quizfight.common.SocketConnection
import org.quizfight.common.messages.*
import java.io.Serializable
import java.net.ServerSocket
import java.net.Socket

internal class MasterServerTest {

    val MASTER_PORT      = 34567
    val GAME_SERVER_PORT = 45678
    //var tms= MasterServer(MASTER_PORT)
    //val tgs =  GameServer("localhost", GAME_SERVER_PORT, MASTER_PORT)

    @BeforeEach
    fun setUp() {

    }

    @AfterEach
    fun tearDown() {
    }

    @Test
    fun testConnectToMS(){
        println("Test Start")

        GlobalScope.launch {
            println("MS tartet")
            val ms = MasterServer(MASTER_PORT)

        }
        Thread.sleep(1000)

        GlobalScope.launch {
            println("GameServer startet")
            Thread.sleep(1000)
            var gameServer = GameServer("localhost", GAME_SERVER_PORT, MASTER_PORT)

        }
        //Thread.sleep(1000)




        GlobalScope.launch {
            Thread.sleep(5000)
            println("Start Client")
            var tc = TestClient()
        }










        Thread.sleep(20_000)
        println("Test erfolgreich")

    }

}


class TestClient {

    private val MASTER_PORT      = 34567
    private val GAME_SERVER_PORT = 45678

    var msConn = SocketConnection(Socket("localhost", MASTER_PORT),
            mapOf(MsgGameList ::class to { msConn, msg -> showGames((msg as MsgGameList).games) },
                MsgTransferToGameServer::class to { _, msg -> handleServerTransfer(msg as MsgTransferToGameServer) }

                ))


    init {
        createGame()
        requestOpenGames()
    }

    private fun handleServerTransfer(msg: MsgTransferToGameServer) {
        msConn?.close()
        val oldHandlers = msConn?.handlers ?: emptyMap()
        val serverData = msg.gameServer
        val socket = Socket(serverData.ip, serverData.port)
        msConn = SocketConnection(socket, oldHandlers)
        createGame()

    }










    fun requestOpenGames(){
        println("Request open games: ")
        msConn.send(MsgRequestOpenGames())
    }

    fun showGames(gameList: List<GameData>) {
        println("Open Games: ")
        gameList.forEach{ println(it) }
    }


    fun createGame(){
        val gameRequest = GameRequest("UnitTestGame", 2, 5)
                msConn.send(MsgCreateGame(gameRequest, "Knecht"))

    }

    fun joinGame(){

    }


}