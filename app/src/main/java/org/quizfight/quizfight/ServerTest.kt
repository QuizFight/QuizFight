package org.quizfight.quizfight

import org.quizfight.common.Connection
import org.quizfight.common.SocketConnection
import org.quizfight.common.messages.*
import org.quizfight.common.question.Category
import org.quizfight.common.question.ChoiceQuestion
import java.net.ServerSocket

class Server(val port: Int) {

    val socket = ServerSocket(port)
    val conn : Connection
    val questionList = arrayListOf<ChoiceQuestion> ()
    var count : Int = 1
    var score : Int = 0

    init {
        initQuestionList()
        conn = SocketConnection(socket.accept(), mapOf(
                MsgStartGame ::class to { conn, msg -> handleGameStart(msg as MsgStartGame)},
                MsgScore::class to { conn, msg -> handleMsgScore(msg as MsgScore) },
                MsgCreateGame::class to { conn, msg -> handleMsgCreateGame(msg as MsgCreateGame) },
                MsgRequestOpenGames::class to { conn, msg -> handleMsgRequestOpenGames(msg as MsgRequestOpenGames)},
                MsgJoin::class to { conn, msg -> handleMsgJoin() }
        ))

    }

    fun handleMsgScore(msg: MsgScore) {
        println("Answer received! The new score is ${msg.score}")
        score += msg.score
        val map = mapOf(Pair("Anana", score), Pair("Coriano", 600), Pair("Bluenote", 1000))
        val sorted = map.toSortedMap()
        conn.send(MsgRanking(sorted))
        Thread.sleep(10000)
        if(count < questionList.size) {
            conn.send(MsgQuestion(questionList[count]))
            count++
        }
    }

    fun handleMsgCreateGame(msg: MsgCreateGame){
        println("create game")

        val gameInfo: GameData = GameData("1", "TestGame", 5, listOf("aude", "rapha"), 5)

        conn.send(MsgGameInfo(gameInfo))

    }


    fun handleGameStart(msg: MsgStartGame) {
        println("start game")
        conn.send(MsgQuestion(questionList[0]))
    }


    fun handleMsgRequestOpenGames(msg: MsgRequestOpenGames){

        val gameInfo: GameData = GameData("1", "TestGame", 5, listOf("aude", "rapha"), 5)
        val gameInfo2: GameData = GameData("3", "TestGame2", 7, listOf("aude", "rapha"), 5)
        val gameInfo3: GameData = GameData("2", "TestGame3", 3, listOf("aude", "rapha"), 5)

        var gameList = listOf<GameData>( gameInfo,gameInfo2, gameInfo3)

        conn.send(MsgGameList(gameList))

    }

    fun handleMsgJoin(){
        println("join game")
        conn.send(MsgQuestion(questionList[0]))
    }

    fun initQuestionList() {
        val q0 = ChoiceQuestion("Welcher dieser Herren ist Teil des Videospielunternehmens Nintendo? ",
                Category.AROUND_THE_WORLD,
                listOf( "Hillenburg",
                        "Turing",
                        "Schmitz"),
                "Shigeru Miyamoto")

        val q1 = ChoiceQuestion("In welchem Jahr erschien in Deutschland das Super Nintendo Entertainment System? ",
                Category.AROUND_THE_WORLD,
                listOf("1992",
                        "1991",
                        "1993"),
                "1990")

        val q2 = ChoiceQuestion("welchem Land steht der schiefe Turm von Pisa?",
                Category.AROUND_THE_WORLD,
                listOf("Italien",
                        "Portugal",
                        "Bulgarien"),
                "Turmland")

        val q3 = ChoiceQuestion("Wie lautet die Hauptstadt von Weissrussland?",
                Category.AROUND_THE_WORLD,
                listOf("Minsk",
                        "Tallinn",
                        "Riga"),
                "Nimsk")

        val q4 = ChoiceQuestion("Wann wurde offiziell der EURO eingefuehrt?",
                Category.AROUND_THE_WORLD,
                listOf("1. Januar 2002",
                        "1. Januar 2001",
                        "12. Januar 2001"),
                "12. Januar 2002")
        val q5 = ChoiceQuestion("Wann wurde offiziell der EURO eingefuehrt?",
                Category.AROUND_THE_WORLD,
                listOf("1. Januar 2002",
                        "1. Januar 2001",
                        "12. Januar 2001"),
                "12. Januar 2002")

        questionList.addAll(listOf(q0,q1, q2,q3,q4,q5))
    }
}



fun main(args: Array<String>) {

    val server = Server(23456)

    while(true);
}




