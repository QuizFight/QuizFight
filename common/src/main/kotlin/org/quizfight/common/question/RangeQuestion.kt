package org.quizfight.common.question

/**
 * Class for building ranged questions for estimation
 * @author Julian Einspenner
 */
class RangeQuestion(text: String, category: String, type: Type, correctAnswer: String, val begin: String, val end: String)
      : Question(text, category, type, correctAnswer)