package org.quizfight.server

import org.quizfight.questionStore.QuestionStore

/**
 * Starts the Server
 * You can choose between new Master Server and Slave Server
 */
fun main(args: Array<String>){
    println("Start MasterServer (m), GameServer(s), or exit (e)?")
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


}