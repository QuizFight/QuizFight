package org.quizfight.questions

/**
 * Class for building ranged questions for estimation
 * @author Julian Einspenner
 */
class RangeQuestion(category: String, type: String, correctAnswer: String, val begin: Double, val end: Double)
      : Question(category, type, correctAnswer) {

}