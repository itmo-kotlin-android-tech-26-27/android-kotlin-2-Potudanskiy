package com.example.quiz.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.quiz.Answer
import com.example.quiz.Attempt
import com.example.quiz.QuizViewModel
import com.example.quiz.Screen

/** Отчет: сверху итог, ниже прокручиваемый разбор каждого вопроса. */
@Composable
fun ReportScreen(vm: QuizViewModel, report: Screen.Report) {
    val attempt = report.attempt

    LazyColumn(verticalArrangement = Arrangement.spacedBy(SMALL_SPACE)) {
        item { ReportSummary(report.login, attempt, onBack = { vm.back() }) }
        if (report.returnTo != Screen.AdminAttempts) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(SMALL_SPACE)) {
                    Button(
                        onClick = { vm.startTest(attempt.topic) },
                        modifier = Modifier.weight(1f),
                    ) { Text("Пройти заново") }
                    OutlinedButton(
                        onClick = { vm.open(Screen.Topics) },
                        modifier = Modifier.weight(1f),
                    ) { Text("К темам") }
                }
            }
        }
        item { Text("Разбор вопросов", style = MaterialTheme.typography.titleMedium) }
        if (attempt.answers.isEmpty()) {
            item { EmptyMessage("В этой попытке нет ответов.") }
        }
        itemsIndexed(attempt.answers) { index, answer -> AnswerCard(index + 1, answer) }
    }
}

@Composable
private fun ReportSummary(login: String, attempt: Attempt, onBack: () -> Unit) {
    Column {
        ScreenHeader("Отчёт о попытке", onBack = onBack)
        Text("Студент: $login")
        Text("Дата: ${attempt.date}")
        Text("Статус: ${attempt.status}")
        Text("Балл: ${attempt.score}", style = MaterialTheme.typography.titleMedium)
        Text("Верно: ${attempt.score}, неверно: ${attempt.wrongCount}, время вышло: ${attempt.timeOutCount}")
        Text(
            "По темам",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = SMALL_SPACE),
        )
        Text("${attempt.topic}: верно ${attempt.score} из ${attempt.answers.size}")
    }
}

/** Разбор одного вопроса: формулировка, ответ студента, правильный ответ, результат. */
@Composable
private fun AnswerCard(number: Int, answer: Answer) {
    val question = answer.question
    val given = if (answer.given.isBlank()) "—" else question.answerText(answer.given)
    val result = when (answer.isCorrect) {
        true -> "верно"
        false -> "неверно"
        null -> "время вышло"
    }
    val resultColor = if (answer.isCorrect == true) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.error
    }
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(LARGE_SPACE)) {
            Text("$number. ${question.text}", style = MaterialTheme.typography.titleSmall)
            Text("Ответ студента: $given")
            Text("Правильный ответ: ${question.answerText(question.correctAnswer)}")
            Text("Результат: $result", color = resultColor)
        }
    }
}
