package org.quizfight.questionStore

import org.quizfight.common.question.FourAnswersQuestion
import org.quizfight.common.question.Question
import org.quizfight.common.question.Types

/**
 * Random selection of questions from the question list of the QuestionStore
 * Comfortable selecting a random count of questions, which distinguish from each other
 * by type or category. Awesome gaming experience guaranteed!
 * @author Julian Einspenner
 */
class QuestionSelector {

    /**
     * Returns a single question, just for prototyping
     * @param list is the list from QuestionStore
     * @return is the random question. If no question was found a hardcoded one will be chosen
     */
    fun getQuestionForPrototypeTesting(list: List<Question>): Question {
        if(list.size == 0)
            return FourAnswersQuestion("Fragetext", "Kategorie", Types.FOUR_ANSWERS.id,
                    "Korrekte Anwort", "Falsch 1", "Falsch 2", "Falsch 3")

        var question: Question
        do{
            val randomIndex = (0..list.size).random()
            question = list.get(randomIndex)
        }while(question.type != Types.FOUR_ANSWERS.id)

        return question
    }

    /**
     * Returns a question list for a whole game. The count of questions is limited
     * @param count is the count of questions a game will use
     * @return is the list of questions. Its size is obtained by param count
     */
    fun getQuestionsForGame(count: Int, list: List<Question>): List<Question>{
        if(count > list.size || count < 4 || count > 20) return listOf<Question>()

        var tmpType = "tmpType"
        var tmpCategory = "tmpCategory"

        var copyOfList = list.toMutableList()
        var listForGame = mutableListOf<Question>()

        var newQuestion: Question
        var random: Int

        for(i in 1..count){
            do{
                random = (0..copyOfList.size-1).random()
                newQuestion = copyOfList.get(random)
            }while(tmpType == newQuestion.type && tmpCategory == newQuestion.category && copyOfList.size != 1)

            copyOfList.removeAt(random)
            listForGame.add(newQuestion)

            tmpType = newQuestion.type
            tmpCategory = newQuestion.category
        }
        return listForGame
    }
}
