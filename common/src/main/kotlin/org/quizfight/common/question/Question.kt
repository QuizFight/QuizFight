package org.quizfight.common.question

import java.io.Serializable

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
    fun evaluate(answer: T): Int
}

class ChoiceQuestion (
        override val text: String,
        override val category: Category,
        val choices: List<String>,
        val correctChoice: String
) : Question<String> {
    //TODO: Impelement
    override fun evaluate(answer: String) = if (answer == correctChoice ) 100 else 0
}

class GuessQuestion(
        override val text: String,
        override val category: Category,
        val lowest: Int,
        val highest: Int,
        val correctValue: Int
) : Question<Int> {
    override fun evaluate(answer: Int): Int {
        //TODO: Impelement
        return 9001
    }
}

