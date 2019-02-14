package org.quizfight.server

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.quizfight.common.messages.MsgJoinGame
import org.quizfight.common.messages.MsgRequestAllGames
import org.quizfight.common.question.FourAnswersQuestion
import org.quizfight.common.question.Type
import org.quizfight.questionStore.QuestionStore
import javax.xml.bind.JAXBElement

/**
 * Starts the Server
 * You can choose between new Master Server and Slave Server
 */
fun main(args: Array<String>){

    GlobalScope.launch {
        MasterServer(1)
    }


    Thread.sleep(3000)

    val gs = GameServer("localhost", 2)
    gs.addNewGame(gameName = "Test", maxPlayer = 2, questions = mutableListOf(FourAnswersQuestion("Wer?", "politics", Type.FOUR_ANSWERS_QUESTION, "Ich", "Du", "Er", "Sie")))


    GlobalScope.launch {
        delay(2000)
        val client = Client("localhost", 1)
        client.connection.send(MsgRequestAllGames())


        delay(2000)
        client.connection.send(MsgJoinGame(client.games[0].id, "Udo"))

        delay(2000)
        client.connection.send(MsgJoinGame(client.games[0].id, "Udo"))

        while(true);

    }

    while(true);
    //-------Prototyp Test---------//

    //val store = QuestionStore()
    //val questions = store.getQuestionsForGame(5).toMutableList()

    //gs.addNewGame("testGame", 5, questions)
    //var game = gs.games.find { it.gameName == "testGame" }
    //println("Das Game hat folgende Fragen:\n")

    //game?.printQuestions()

    //gs.start()

    /*println("Start MasterServer (m), GameServer(s), or exit (e)?")
    var stringInput = readLine()

    while (!stringInput.equals("e")){
        if(stringInput.equals("m")){
            println("Master Server IP-Address: ")
            val mip = readLine()
            if (!mip.isNullOrEmpty()){
                val ms = MasterServer()
                ms.start()
            }
        } else if (stringInput.equals("s")){
            println("Master Server IP-Address: ")
            val mip = readLine()

            println("Slave Server IP-Address: ")
            val ip = readLine()
            if (!ip.isNullOrEmpty() && !mip.isNullOrEmpty()){
                val sls = GameServer()

                //-------Prototyp Test---------//
                val store = QuestionStore()
                val questions = store.getQuestionsForGame(5).toMutableList()

                sls.addNewGame("testGame", 5, questions)
                var game = sls.games.find { it.gameName == "testGame" }
                println("Das Game hat folgende Fragen:\n")
                game!!.printQuestions()

                sls.start()



            }
        } else {
            println("Wrong input...")
        }
        stringInput = readLine()
    }

*/
}