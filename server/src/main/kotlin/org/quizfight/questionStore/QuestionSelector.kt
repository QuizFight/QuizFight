package org.quizfight.questionStore

import org.quizfight.common.question.*

/**
 * Random selection of questions from the question list of the QuestionStore
 * Comfortable selecting a random count of questions, which distinguish from each other
 * by type or category. Awesome gaming experience guaranteed!
 * @author Julian Einspenner
 */
class QuestionSelector {

    /**
     * Returns a question list for a whole game. The count of questions is limited
     * @param count is the count of questions a game will use
     * @return is the list of questions. Its size is obtained by param count
     */
    fun getQuestionsForGame(count: Int, list: List<Question<*>>): List<Question<*>>{
        if(count > list.size || count < 1 || count > 20) return listOf<Question<*>>()

        var tmpType = "tmpType"
        var tmpCategory = "tmpCategory"

        var copyOfList = list.toMutableList()
        var listForGame = mutableListOf<Question<*>>()

        var newQuestion: Question<*>
        var random: Int

        for(i in 1..count){
            do{
                random = (0..copyOfList.size-1).random()
                newQuestion = copyOfList.get(random)
            }while(tmpType == newQuestion.javaClass.name && tmpCategory == newQuestion.category.name && copyOfList.size != 1)

            copyOfList.removeAt(random)
            listForGame.add(newQuestion)

            tmpType = newQuestion.javaClass.name
            tmpCategory = newQuestion.category.name
        }
        return listForGame
    }
}
