package com.example.quiz

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val DATE_FORMAT = "dd.MM.yyyy HH:mm"

/**
 * Прохождение одной темы в режиме «лестницы».
 * Объект неизменяемый: каждый шаг возвращает новую копию через copy().
 */
data class TestSession(
    val topic: String,
    private val questions: List<Question>,
    private val maxQuestions: Int,
    val current: Question? = null,
    private val asked: List<Question> = emptyList(),
    private val answers: List<Answer> = emptyList(),
    private val level: Int = FIRST_LEVEL,
    private val topicFinished: Boolean = false,
    private val date: String = SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(Date()),
) {
    val number: Int get() = asked.size
    val maxLevel: Int get() = questions.maxOfOrNull { it.level } ?: FIRST_LEVEL
    val isAnswered: Boolean get() = answers.size == asked.size

    /** Записывает ответ на текущий вопрос и сдвигает ступень: верно — вверх, неверно — вниз. */
    fun answer(given: String): TestSession {
        val question = current ?: return this
        if (isAnswered || !question.isValidAnswer(given)) return this
        val correct = question.isCorrect(given)
        return copy(
            answers = answers + Answer(question, given, correct),
            level = if (correct) question.level + 1 else maxOf(FIRST_LEVEL, question.level - 1),
            topicFinished = (correct && question.level == maxLevel) || asked.size >= maxQuestions,
        )
    }

    /** Показывает следующий вопрос. Если тема закончилась, current становится null. */
    fun next(): TestSession {
        if (!isAnswered) return this
        if (topicFinished) return copy(current = null)
        val question = pickQuestion() ?: return copy(current = null)
        return copy(current = question, asked = asked + question)
    }

    /** Собирает попытку. Если время вышло, неотвеченный вопрос попадает в отчет как «время вышло». */
    fun toAttempt(status: String): Attempt {
        val question = current
        val timedOut = if (status == Attempt.TIME_OUT && question != null && !isAnswered) {
            listOf(Answer(question, "", null))
        } else {
            emptyList()
        }
        return Attempt(date, topic, status, answers + timedOut)
    }

    /** Случайный еще не заданный вопрос нужного уровня. Нет такого — ближайший ниже, потом ближайший выше. */
    private fun pickQuestion(): Question? {
        val free = questions.filter { it !in asked }
        if (free.isEmpty()) return null
        val lower = free.filter { it.level <= level }
        val chosenLevel = if (lower.isNotEmpty()) lower.maxOf { it.level } else free.minOf { it.level }
        return free.filter { it.level == chosenLevel }.random()
    }
}
