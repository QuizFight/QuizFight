package org.quizfight.server

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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



    val gs = GameServer(2)


    //-------Prototyp Test---------//

    val store = QuestionStore()
    val questions = store.getQuestionsForGame(5).toMutableList()

    gs.addNewGame("testGame", 5, questions)
    var game = gs.games.find { it.gameName == "testGame" }
    println("Das Game hat folgende Fragen:\n")

    game?.printQuestions()

    gs.start()

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