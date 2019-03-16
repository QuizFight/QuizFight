package org.quizfight.common.question

import org.quizfight.common.messages.Message
import java.io.Serializable;


enum class Category {
    ANIMALS,
    AROUND_THE_WORLD,
    CHEMISTRY,
    COMPUTER_SCIENCE,
    FUN,
    GAMING,
    HISTORY,
    MOVIES,
    RELIGION,
    SPORTS
}

interface Question<T> : Serializable {
    val text: String
    val category: Category
    fun evaluate(answer: T, usedTime: Int, totalTime: Int): Int
}

class ChoiceQuestion (
        override val text: String,
        override val category: Category,
        val choices: List<String>,
        val correctChoice: String
) : Question<String> {
    //TODO: Impelement
    override fun evaluate(answer: String, usedTime: Int, totalTime: Int):Int {
        var score:Double = 0.0
        if(answer == correctChoice) {
            score= ((totalTime.toDouble() - usedTime.toDouble()) / totalTime.toDouble() * 500) + 500    //Max Score = 1000, min Score = 500
            return score.toInt()
        } else {
            return 0
        }
    }//= if (answer == correctChoice ) 100 else 0
}

class GuessQuestion(
        override val text: String,
        override val category: Category,
        val lowest: Int,
        val highest: Int,
        val correctValue: Int
) : Question<Int> {
    override fun evaluate(answer: Int, usedTime: Int, totalTime: Int): Int {
        var score:Double = 0.0
        if(answer == correctValue) {
            return 1000
        }
        if( answer < correctValue) {
            score = (answer - lowest).toDouble() / (correctValue - lowest).toDouble() * 1000
            return score.toInt()
        } else {
            score = (highest - answer).toDouble() / (highest - correctValue).toDouble() * 1000
            return score.toInt()
        }
    }
}

