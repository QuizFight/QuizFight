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
import java.lang.Exception
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

        GlobalScope.launch {
            Thread.sleep(5000)
            println("Start Client")
            var tc = TestClient()
        }

        Thread.sleep(20_000)
        println("Test erfolgreich")

    }

}

/**
 * TestClient 2. Versuch
 */
class TC {
    private val MASTER_SERVER_PORT  = 34567
    private val GAME_SERVER_PORT    = 45678

    private lateinit var conn : SocketConnection
    private lateinit var allOpenGames : List<GameData>

    init {
        try{
            conn = SocketConnection(Socket("localhost", GAME_SERVER_PORT),
                    mapOf(MsgGameList ::class to { _, msg -> showGames((msg as MsgGameList).games)}  ))
        } catch (e: Exception) {
            e.printStackTrace()
        }

        sendRequestOpenGame()
    }

    fun sendRequestOpenGame(){
        println("Send Request")
        conn.send(MsgRequestOpenGames())

    }

    fun showGames(gameList: List<GameData>){
        println(gameList)
    }
}


/**
 * TestClient 1. Versuch
 */
class TestClient {

    private val MASTER_PORT      = 34567
    private val GAME_SERVER_PORT = 45678

    /**
     * Connection zu Master Server
     */
    /*var msConn = SocketConnection(Socket("localhost", MASTER_PORT),
            mapOf(MsgGameList ::class to { msConn, msg -> showGames((msg as MsgGameList).games) },
                MsgTransferToGameServer::class to { _, msg -> handleServerTransfer(msg as MsgTransferToGameServer) }

                ))*/
    lateinit var gsConn:SocketConnection

    /**
     * Konstruktor, erstelle ein Spiel und erfrage dann alle Spiele
     */
    init {
       /*GlobalScope.launch { gsConn = SocketConnection(Socket("localhost", GAME_SERVER_PORT),
                mapOf(MsgGameList ::class to { msConn, msg -> showGames((msg as MsgGameList).games) }//,
                        //MsgTransferToGameServer::class to { _, msg -> handleServerTransfer(msg as MsgTransferToGameServer) }

                )) }*/
        gsConn = SocketConnection(Socket("localhost", GAME_SERVER_PORT), mapOf())
        createGame()
        //Thread.sleep(5000)
        requestOpenGames()
    }

    /**
     * Unnötig
     *//*
    private fun handleServerTransfer(msg: MsgTransferToGameServer) {
        msConn?.close()
        println("Tausche Connection")
        val oldHandlers = msConn?.handlers ?: emptyMap()
        val serverData = msg.gameServer
        val socket = Socket(serverData.ip, serverData.port)
        msConn = SocketConnection(socket, oldHandlers)
        //createGame()

    }*/


    fun requestOpenGames(){
        println("Request open games: ")
        gsConn.send(MsgRequestOpenGames())
    }

    fun showGames(gameList: List<GameData>) {
        println("Open Games: ")
        gameList.forEach{ println(it) }
    }


    fun createGame(){
        val gameRequest = GameRequest("UnitTestGame", 2, 5)
        println("Erstelle Spiel")
        gsConn.send(MsgCreateGame(gameRequest, "Knecht"))

    }

    fun joinGame(){

    }



}