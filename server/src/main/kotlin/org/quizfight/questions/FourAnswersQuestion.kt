package org.quizfight.questions

/**
 * Class for building Four-Answer-Questions
 * @author Julian Einspenner
 */
class FourAnswersQuestion: Question{

    val badAnswer_1: String
    val badAnswer_2: String
    val badAnswer_3: String

    constructor(category: String, type: Int, correctAnswer: String,
                badAnswer_1: String, badAnswer_2: String, badAnswer_3: String): super(category, type, correctAnswer){
        this.badAnswer_1 = badAnswer_1
        this.badAnswer_2 = badAnswer_2
        this.badAnswer_3 = badAnswer_3
    }
}