package org.quizfight.common.question

import org.quizfight.common.messages.Message

enum class Category {
    //TODO: Impelement
    POLITICS,
    BADGER,
    MUSHROOM
}

interface Question<T> {
    val text: String
    val category: Category
    fun evaluate(answer: T): Int
}

class ChoiceQuestion(
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
        val range: IntRange,
        val correctValue: Int
) : Question<Int> {
    override fun evaluate(answer: Int): Int {
        //TODO: Impelement
        return 9001
    }
}

