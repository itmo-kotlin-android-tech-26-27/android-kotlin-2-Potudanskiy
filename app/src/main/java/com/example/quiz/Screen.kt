package com.example.quiz

/** Все экраны приложения. Какой сейчас открыт — хранится в QuizViewModel.screen. */
sealed class Screen {
    data object Login : Screen()
    data object Topics : Screen()
    data object Quiz : Screen()
    data object History : Screen()
    data class Report(val login: String, val attempt: Attempt, val returnTo: Screen = Topics) : Screen()
    data object Admin : Screen()
    data object AdminTopics : Screen()
    data class AdminQuestions(val topic: String) : Screen()
    data class AdminQuestion(val question: Question) : Screen()
    data object AdminSettings : Screen()
    data object AdminAttempts : Screen()
}
