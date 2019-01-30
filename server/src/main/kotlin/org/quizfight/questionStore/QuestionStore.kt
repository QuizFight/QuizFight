package org.quizfight.questionStore

import org.quizfight.common.question.Question
import org.quizfight.common.question.Types

/**
 * The QuestionStore saves the list of questions for every Game.
 * It will be able to refresh the question list while the server is running.
 * This will allow to add new questions to the database without termination.
 * Furthermore the Store can ask the QuestionSelector for questions of a chosen type
 * or a chosen category
 * @author Julian Einspenner
 */
class QuestionStore (var questions: List<Question>){

    /**
     * Allows the refreshment of the loaded question list
     * @param questions is the new list of Question-objects
     */
    fun refreshQuestionList(questions: List<Question>){
        this.questions = questions
    }

    /**
     * Returns the size of the loaded questions
     * @return the count of loaded question
     */
    fun getSize(): Int = questions.size

    /**
     * Counts the questions from concrete type
     * @param ID is the type of a question.
     * @see Question for ID Info
     * @return count is the count of the questions of this type
     */
    fun countQuestionsOfType(ID: String): Int{
        var count = 0
        for(question in questions){
            if(question.type == ID) count++
        }
        return count
    }

    /**
     * Creates an information string about the currently loaded questions in the QuestionStore
     * Information includes: Count, Count of every Type
     * @return information is the information String
     */
    override fun toString(): String{
        return "Counted " + getSize() + " questions\n" +
               "Type FourAnswers: "   + countQuestionsOfType(Types.FOUR_ANSWERS.id)     + "\n" +
               "Type Ranged: "        + countQuestionsOfType(Types.RANGED_QUESTIONS.id) + "\n"
    }

}