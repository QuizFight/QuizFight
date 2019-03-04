package org.quizfight.server

import org.quizfight.common.question.Question
import org.quizfight.questionStore.QuestionStore

/**
 * Starts the Server
 * You can choose between new Master Server and Slave Server
 */
fun main(args: Array<String>){
    val gs = GameServer()
    gs.addNewGame("1", "testGame", 2, 5)
    gs.games[0].printQuestions()
    gs.start() // muss zuallererst passieren. danach kann aber aktuell kein Game hinzugefügt werden

    //-------Prototyp Test---------//
/*
    val store = QuestionStore()
    val questions = store.getQuestionsForGame(5).toMutableList()

    gs.addNewGame("testGame", 5, 5)
    var game = gs.games.find { it.gameName == "testGame" }
    println("Das Game hat folgende Fragen:\n")
    game!!.printQuestions()

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

*/*/
}