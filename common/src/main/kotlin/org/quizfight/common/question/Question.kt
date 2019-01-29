package org.quizfight.common.question

import org.quizfight.common.messages.Message

/**
 * This class represents a data-sructure to handle and organize questions.
 * Every class will have a category, a type and a correct answer.
 * @author Julian Einspenner
 * @param category is the category of a question
 * @param type is the type of a question. In QuestionUtils-class is a corresponding enumeration
 * @param correctAnswer is the only correct solution of a question
 */
open class Question(val text: String, val category: String, val type: String, val correctAnswer: String): Message

enum class Types(var id: String){
    FOUR_ANSWERS("fourAnswerQuestion"),
    RANGED_QUESTIONS("rangedQuestion")
}
