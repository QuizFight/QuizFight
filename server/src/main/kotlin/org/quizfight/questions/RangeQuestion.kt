package org.quizfight.questions

/**
 * Class for building ranged questions for estimation
 * @author Julian Einspenner
 */
class RangeQuestion: Question {

    val begin: Double
    val end: Double

    constructor(category: String, type: Int, correctAnswer: String,
                begin: Double, end: Double): super(category, type, correctAnswer){
        this.begin = begin
        this.end   = end
    }

}