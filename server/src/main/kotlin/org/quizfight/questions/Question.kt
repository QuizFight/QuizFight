package org.quizfight.questions

/**
 * This class represents a data-sructure to handle and organize questions.
 * Every class will have a category, a type and a correct answer.
 * @author Julian Einspenner
 * @param category is the category of a question
 * @param type is the type of a question. In QuestionUtils-class is a corresponding enumeration
 * @param correctAnswer is the only correct solution of a question
 */
open class Question(val category: String, val type: String, val correctAnswer: String)
