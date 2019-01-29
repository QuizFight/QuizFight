package org.quizfight.questions

/**
 * This class represents a data-sructure to handle and organize questions.
 * Every class will have a category, a type and a correct answer.
 * @author Julian Einspenner
 */
open class Question{

    val category: String
    val type: Int
    val correctAnswer: String

    /**
     * Constructor for Questions
     * @param category is the category of a question
     * @param type is the type of a question. In QuestionUtils-class is a corresponding enumeration
     * @param correctAnswer is the only correct solution of a question
     */
    constructor(category: String, type: Int, correctAnswer: String){
        this.category = category
        this.type = type
        this.correctAnswer = correctAnswer
    }
}