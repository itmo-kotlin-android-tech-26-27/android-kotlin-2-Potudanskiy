package com.example.quiz.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.quiz.QuizViewModel
import com.example.quiz.Screen

/** Корневой экран: по vm.screen решает, какой экран показать. */
@Composable
fun QuizApp(modifier: Modifier = Modifier, vm: QuizViewModel = viewModel()) {
    // Системная кнопка «Назад». На экране входа выключена — тогда приложение просто закрывается.
    BackHandler(enabled = vm.screen != Screen.Login) { vm.back() }

    Box(modifier.fillMaxSize().padding(LARGE_SPACE)) {
        when (val screen = vm.screen) {
            Screen.Login -> LoginScreen(vm)
            Screen.Topics -> TopicsScreen(vm)
            Screen.Quiz -> QuizScreen(vm)
            Screen.History -> HistoryScreen(vm)
            is Screen.Report -> ReportScreen(vm, screen)
            Screen.Admin -> AdminScreen(vm)
            Screen.AdminTopics -> AdminTopicsScreen(vm)
            is Screen.AdminQuestions -> AdminQuestionsScreen(vm, screen.topic)
            is Screen.AdminQuestion -> AdminQuestionScreen(vm, screen.question)
            Screen.AdminSettings -> AdminSettingsScreen(vm)
            Screen.AdminAttempts -> AdminAttemptsScreen(vm)
        }
    }
}
