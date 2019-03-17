package org.quizfight.common.test


import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.quizfight.common.question.Category
import org.quizfight.common.question.ChoiceQuestion
import org.quizfight.common.question.GuessQuestion
import java.util.*

internal class QuestionTest {
    var cat= Category.GAMING
    val choices1= listOf<String>("1", "2", "3", "4")
    val choiceQuestion = ChoiceQuestion("Testfrage1", cat, choices1, "1")
    val guessQuestion = GuessQuestion("GuessQuestion", cat, 10, 50,30)
    val guessQuestion2 = GuessQuestion("GuessQuestion2", cat, 10, 50,10)
    val guessQuestion3 = GuessQuestion("GuessQuestion2", cat, 10, 50,50)


    @BeforeEach
    fun setUp() {
    }

    @AfterEach
    fun tearDown() {
    }

    @Test
    fun evaluate() {
        assertEquals(880, choiceQuestion.evaluate("1", 5, 21))
        assertEquals(0, choiceQuestion.evaluate("2", 5, 21))
        assertEquals(500, choiceQuestion.evaluate("1", 21, 21))
        assertEquals(1000, choiceQuestion.evaluate("1", 0, 21))
        assertEquals(523, choiceQuestion.evaluate("1", 20, 21))


        assertEquals(1000, guessQuestion.evaluate(30, 5, 21))
        assertEquals(0, guessQuestion.evaluate(10, 5, 21))
        assertEquals(0, guessQuestion.evaluate(50, 5, 21))
        assertEquals(500, guessQuestion.evaluate(20, 5, 21))
        assertEquals(500, guessQuestion.evaluate(40, 5, 21))
        assertEquals(250, guessQuestion.evaluate(15, 5, 21))

        assertEquals(1000, guessQuestion2.evaluate(10, 5, 21))
        assertEquals(1000, guessQuestion3.evaluate(50, 5, 21))

    }

    @Test
    fun getText() {
    }

    @Test
    fun getCategory() {
        assertEquals(true,guessQuestion.category== Category.GAMING)
        assertEquals(false, choiceQuestion.category == Category.HISTORY)
    }

    @Test
    fun getChoices() {
    }

    @Test
    fun getCorrectChoice() {
    }
}