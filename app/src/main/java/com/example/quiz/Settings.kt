package com.example.quiz

import kotlinx.serialization.Serializable

@Serializable
data class Settings(
    val timeLimitSeconds: Int = DEFAULT_TIME_SECONDS,
    val maxQuestionsPerTopic: Int = DEFAULT_MAX_QUESTIONS,
) {
    init {
        require(timeLimitSeconds in MIN_TIME_SECONDS..MAX_TIME_SECONDS) {
            "Время на тест: от $MIN_TIME_SECONDS до $MAX_TIME_SECONDS секунд."
        }
        require(maxQuestionsPerTopic in MIN_QUESTIONS..MAX_QUESTIONS) {
            "Вопросов на тему: от $MIN_QUESTIONS до $MAX_QUESTIONS."
        }
    }

    companion object {
        const val MIN_TIME_SECONDS = 10
        const val MAX_TIME_SECONDS = 86_400
        const val MIN_QUESTIONS = 1
        const val MAX_QUESTIONS = 100
        private const val DEFAULT_TIME_SECONDS = 300
        private const val DEFAULT_MAX_QUESTIONS = 8
    }
}
