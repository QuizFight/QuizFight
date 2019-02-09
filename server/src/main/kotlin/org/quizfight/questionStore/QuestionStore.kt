package org.quizfight.questionStore

import org.quizfight.common.question.Question
import org.quizfight.common.question.Type
import org.quizfight.parser.XmlParser

/**
 * The QuestionStore saves the list of questions for every Game.
 * It will be able to refresh the question list while the server is running.
 * This will allow to add or remove questions to the database without termination.
 * Furthermore the Store can ask the QuestionSelector for questions of a chosen type
 * or a chosen category
 * @author Julian Einspenner
 */
class QuestionStore{

    var questions = mutableListOf<Question>()

    constructor(){
        refreshQuestionList()
    }

    /**
     * Allows the refreshment by reloading the Xml-Files
     */
    fun refreshQuestionList(){
        questions = XmlParser().convertXmlToQuestions().toMutableList()
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
            if(question.type.name == ID) count++
        }
        return count
    }

    /**
     * Returns a single question, just for prototyping
     * @return is the random question. If no question was found a hardcoded one will be chosen
     */
    fun getQuestionForPrototypeTesting(): Question {
        return QuestionSelector().getQuestionForPrototypeTesting(questions)
    }

    /**
     * Returns a question list for a whole game. The count of questions is limited.
     * @param count is the count of questions a game will use
     * @return is the list of questions. Its size is obtained by param count
     */
    fun getQuestionsForGame(count: Int): List<Question>{
        return QuestionSelector().getQuestionsForGame(count, this.questions)
    }

    /**
     * Creates an information string about the currently loaded questions in the QuestionStore
     * Information includes: Count, Count of every Type
     * @return information is the information String
     */
    override fun toString(): String{
        return "Counted " + getSize() + " questions\n" +
               "Type FourAnswers: "   + countQuestionsOfType(Type.FOUR_ANSWERS_QUESTION as String)     + "\n" +
               "Type Ranged: "        + countQuestionsOfType(Type.RANGED_QUESTION as String) + "\n"
    }

}