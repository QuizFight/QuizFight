package org.quizfight.questions

/**
 * Class for building Four-Answer-Questions
 * @author Julian Einspenner
 */
class FourAnswersQuestion(category: String, type: String, correctAnswer: String,
                          val badAnswer_1: String, val badAnswer_2: String, val badAnswer_3: String)
                          : Question(category, type, correctAnswer)