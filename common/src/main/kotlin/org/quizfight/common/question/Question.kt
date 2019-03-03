package org.quizfight.common.question

import org.quizfight.common.messages.Message
import java.io.Serializable;

enum class Category {
    //TODO: Impelement
    GAMING,
    AROUND_THE_WORLD,
    HISTORY,
    CATEGORY1,
    CATEGORY2,
    CATEGORY3
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
        val range: IntRange,
        val correctValue: Int
) : Question<Int> {
    override fun evaluate(answer: Int): Int {
        //TODO: Impelement
        return 9001
    }
}

