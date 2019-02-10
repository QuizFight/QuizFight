package org.quizfight.common.question

/**
 * Class for building Four-Answer-Questions
 * @author Julian Einspenner
 */
class FourAnswersQuestion(text: String, category: String, type: Type, correctAnswer: String,
                          val badAnswer_1: String, val badAnswer_2: String, val badAnswer_3: String)
                          : Question(text, category, type, correctAnswer)