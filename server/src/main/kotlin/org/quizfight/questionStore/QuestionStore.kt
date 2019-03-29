package org.quizfight.questionStore

import org.quizfight.common.question.Question
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

    var questions = mutableListOf<Question<*>>()

    /**
     * Constructor initializes the question store. Store has after calling it an updated question list.
     */
    constructor(){
        refreshQuestionList()
    }

    /**
     * Allows the refreshment by reloading the Xml-Files
     */
    private fun refreshQuestionList(){
        questions = XmlParser().convertXmlToQuestions().toMutableList()
    }

    /**
     * Returns a question list for a whole game. The count of questions is limited.
     * @param count is the count of questions a game will use
     * @return is the list of questions. Its size is obtained by param count
     */
    fun getQuestionsForGame(count: Int): List<Question<*>>{
        return QuestionSelector().getQuestionsForGame(count, this.questions)
    }

    /**
     * Returns the size of the loaded questions
     * @return the count of loaded question
     */
    fun getSize(): Int = questions.size

}