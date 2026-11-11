package com.example.quiz

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

const val FIRST_LEVEL = 1
const val FIRST_LETTER = 'A'
private const val MIN_ITEMS = 2
private val SPACES = Regex("\\s+")

/**
 * Общий родитель всех вопросов. Каждый наследник сам знает,
 * какой ответ допустим, какой верный и как показать ответ в отчете.
 */
@Serializable
sealed class Question {
    abstract val topic: String
    abstract val level: Int
    abstract val text: String

    /** Правильный ответ в том же виде, в каком его дает студент. */
    abstract val correctAnswer: String

    abstract fun isValidAnswer(answer: String): Boolean

    open fun isCorrect(answer: String): Boolean = answer == correctAnswer

    /** Ответ в понятном человеку виде — для отчета. */
    open fun answerText(answer: String): String = answer

    protected fun validateCommon() {
        require(topic.isNotBlank()) { "У вопроса пустая тема." }
        require(text.isNotBlank()) { "У вопроса пустой текст." }
        require(level >= FIRST_LEVEL) { "Уровень вопроса меньше $FIRST_LEVEL." }
    }
}

/** Тестовый вопрос. Ответ — номер варианта, начиная с 1. */
@Serializable
@SerialName("multiple")
data class MultipleChoiceQuestion(
    override val topic: String,
    override val level: Int,
    override val text: String,
    val options: List<String>,
    override val correctAnswer: String,
) : Question() {
    init {
        validateCommon()
        require(options.size >= MIN_ITEMS) { "У тестового вопроса меньше $MIN_ITEMS вариантов." }
        require(isValidAnswer(correctAnswer)) { "Номер правильного варианта вне списка." }
    }

    override fun isValidAnswer(answer: String): Boolean = answer.toIntOrNull() in 1..options.size

    override fun answerText(answer: String): String {
        val option = answer.toIntOrNull()?.let { options.getOrNull(it - 1) } ?: return answer
        return "$answer. $option"
    }
}

/** Открытый вопрос. Регистр и лишние пробелы при проверке не важны. */
@Serializable
@SerialName("open")
data class OpenQuestion(
    override val topic: String,
    override val level: Int,
    override val text: String,
    override val correctAnswer: String,
) : Question() {
    init {
        validateCommon()
        require(correctAnswer.isNotBlank()) { "У открытого вопроса пустой правильный ответ." }
    }

    override fun isValidAnswer(answer: String): Boolean = answer.isNotBlank()

    override fun isCorrect(answer: String): Boolean = normalize(answer) == normalize(correctAnswer)

    private fun normalize(value: String): String = value.trim().lowercase().replace(SPACES, " ")
}

/**
 * Вопрос на соответствие: слева элементы 1, 2, 3..., справа A, B, C...
 * Ответ — строка букв, где i-я буква — пара для i-го элемента слева. Например, "BCA".
 */
@Serializable
@SerialName("match")
data class MatchPairQuestion(
    override val topic: String,
    override val level: Int,
    override val text: String,
    val left: List<String>,
    val right: List<String>,
    override val correctAnswer: String,
) : Question() {
    init {
        validateCommon()
        require(left.size >= MIN_ITEMS && left.size == right.size) { "Столбцы соответствия должны быть одной длины." }
        require(isValidAnswer(correctAnswer)) { "Неверный правильный ответ: $correctAnswer." }
    }

    override fun isValidAnswer(answer: String): Boolean =
        answer.length == left.size &&
            answer.toSet().size == answer.length &&
            answer.all { it - FIRST_LETTER in right.indices }

    override fun answerText(answer: String): String {
        if (!isValidAnswer(answer)) return answer
        return left.indices.joinToString("; ") { "${left[it]} — ${right[answer[it] - FIRST_LETTER]}" }
    }
}
