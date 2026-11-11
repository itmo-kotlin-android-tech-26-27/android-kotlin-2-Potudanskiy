package com.example.quiz

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

class TestSessionTest {
    @Test
    fun nextKeepsUnansweredQuestionAfterDoubleTap() {
        val first = OpenQuestion("Kotlin", 1, "First question", "first")
        val second = OpenQuestion("Kotlin", 2, "Second question", "second")
        val third = OpenQuestion("Kotlin", 3, "Third question", "third")
        val started = TestSession("Kotlin", listOf(first, second, third), maxQuestions = 2).next()
        assertEquals(first, started.current)

        val nextQuestion = started.answer("first").next()
        assertEquals(second, nextQuestion.current)
        assertSame(nextQuestion, nextQuestion.next())

        val finished = nextQuestion.answer("second").next()
        assertNull(finished.current)
        assertEquals(2, finished.toAttempt(Attempt.COMPLETED).score)
    }
}
