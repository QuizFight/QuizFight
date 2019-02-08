package org.quizfight.server

import org.quizfight.common.question.FourAnswersQuestion
import org.quizfight.common.question.Question
import org.quizfight.common.question.Types
import org.quizfight.questionStore.QuestionStore

/**
 * Starts the Server
 * You can choose between new Master Server and Slave Server
 */
fun main(args: Array<String>){


    val gs = GameServer()
    //println("2")
    //-------Prototyp Test---------//

    //val store = QuestionStore()
    //val questions = store.getQuestionsForGame(5).toMutableList()


    // Don't use QuestionStore just yet. It could randomly select RangeQuestions, which the frontend can't handle.
    val questions = mutableListOf<Question>(
            FourAnswersQuestion("Question 1?", "test", Types.FOUR_ANSWERS.id, "correct", "wrong", "wrong", "wrong"),
            FourAnswersQuestion("Question 2?", "test", Types.FOUR_ANSWERS.id, "correct", "wrong", "wrong", "wrong"),
            FourAnswersQuestion("Question 3?", "test", Types.FOUR_ANSWERS.id, "correct", "wrong", "wrong", "wrong"),
            FourAnswersQuestion("Question 4?", "test", Types.FOUR_ANSWERS.id, "correct", "wrong", "wrong", "wrong"),
            FourAnswersQuestion("Question 5?", "test", Types.FOUR_ANSWERS.id, "correct", "wrong", "wrong", "wrong")
    )

    gs.addNewGame("testGame", 5, questions)
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

*/
}