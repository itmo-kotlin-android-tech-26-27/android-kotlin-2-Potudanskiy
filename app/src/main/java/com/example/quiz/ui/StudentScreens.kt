package com.example.quiz.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.quiz.QuizViewModel
import com.example.quiz.Screen

/** Список тем. Нажатие на тему сразу запускает тест. */
@Composable
fun TopicsScreen(vm: QuizViewModel) {
    val login = vm.login ?: return
    val attempts = vm.attemptsOf(login)

    Column(verticalArrangement = Arrangement.spacedBy(SMALL_SPACE)) {
        ScreenHeader("Темы — $login", onBack = { vm.logout() })
        OutlinedButton(
            onClick = { vm.open(Screen.History) },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("История попыток") }

        if (vm.topics.isEmpty()) {
            EmptyMessage("Тем пока нет: в банке нет вопросов.")
        } else {
            Text("Время на тест: ${vm.settings.timeLimitSeconds} сек.")
            // LazyColumn — прокручиваемый список, рисует только видимые элементы.
            LazyColumn(verticalArrangement = Arrangement.spacedBy(SMALL_SPACE)) {
                items(vm.topics) { topic ->
                    val questionCount = vm.questions.count { it.topic == topic }
                    val attemptCount = attempts.count { it.topic == topic }
                    Card(Modifier.fillMaxWidth().clickable { vm.startTest(topic) }) {
                        Column(Modifier.padding(LARGE_SPACE)) {
                            Text(topic, style = MaterialTheme.typography.titleMedium)
                            Text("Вопросов: $questionCount · ваших попыток: $attemptCount")
                        }
                    }
                }
            }
        }
    }
}

/** История попыток текущего студента, новые сверху. */
@Composable
fun HistoryScreen(vm: QuizViewModel) {
    val login = vm.login ?: return
    val attempts = vm.attemptsOf(login).reversed()

    Column {
        ScreenHeader("История попыток", onBack = { vm.back() })
        if (attempts.isEmpty()) {
            EmptyMessage("Попыток пока нет. Пройдите любую тему.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(SMALL_SPACE)) {
                items(attempts) { attempt ->
                    AttemptCard(attempt) { vm.open(Screen.Report(login, attempt)) }
                }
            }
        }
    }
}
