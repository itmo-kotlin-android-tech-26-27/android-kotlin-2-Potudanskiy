package com.example.quiz

import kotlinx.serialization.Serializable

/** Ответ на один вопрос. isCorrect == null — время вышло, ответа нет. */
@Serializable
data class Answer(
    val question: Question,
    val given: String,
    val isCorrect: Boolean?,
) {
    init {
        require(isCorrect == null || question.isValidAnswer(given)) { "Ответ не подходит к вопросу." }
    }
}

/** Попытка по одной теме. Вопросы хранятся внутри ответов, поэтому правки банка не меняют старые отчеты. */
@Serializable
data class Attempt(
    val date: String,
    val topic: String,
    val status: String,
    val answers: List<Answer>,
) {
    init {
        require(status in STATUSES) { "Неизвестный статус попытки: $status." }
    }

    val score: Int get() = answers.count { it.isCorrect == true }
    val wrongCount: Int get() = answers.count { it.isCorrect == false }
    val timeOutCount: Int get() = answers.count { it.isCorrect == null }

    companion object {
        const val COMPLETED = "завершена"
        const val INTERRUPTED = "прервана"
        const val TIME_OUT = "время вышло"
        private val STATUSES = listOf(COMPLETED, INTERRUPTED, TIME_OUT)
    }
}
