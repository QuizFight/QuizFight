package org.quizfight.server

import org.quizfight.questionStore.QuestionStore

class Dialog{

    private val
    private val HELLO_MSG = "Start MasterServer (m), GameServer(s), or quit (q)?"



    init{
        start()
    }


    private fun start(){
        println(HELLO_MSG)
        val selection = readLine()

        while (!selection.equals("q")){
            if(stringInput.equals("m")){
                println("Master Server IP-Address: ")
                val mip = readLine()
                if (!mip.isNullOrEmpty()){
                    val ms = MasterServer(1234)
                }
            } else if (stringInput.equals("s")){
                println("Master Server IP-Address: ")
                val mip = readLine()

                println("Slave Server IP-Address: ")
                val ip = readLine()
                if (!ip.isNullOrEmpty() && !mip.isNullOrEmpty()){
                    val gs = GameServer()

                    //-------Prototyp Test---------//
                    val store = QuestionStore()
                    val questions = store.getQuestionsForGame(5).toMutableList()

                    gs.addNewGame("testGame", 5, questions)
                    var game = gs.games.find { it.gameName == "testGame" }
                    println("Das Game hat folgende Fragen:\n")
                    game!!.printQuestions()

                    gs.start()



                }
            } else {
                println("Wrong input...")
            }
            stringInput = readLine()
        }
    }


}