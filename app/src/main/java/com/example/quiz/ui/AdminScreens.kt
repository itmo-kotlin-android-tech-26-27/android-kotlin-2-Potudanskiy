package com.example.quiz.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.example.quiz.FIRST_LETTER
import com.example.quiz.MatchPairQuestion
import com.example.quiz.MultipleChoiceQuestion
import com.example.quiz.OpenQuestion
import com.example.quiz.Question
import com.example.quiz.QuizViewModel
import com.example.quiz.Screen
import com.example.quiz.Settings

/** Меню администратора: банк вопросов, настройки, попытки, выгрузка. */
@Composable
fun AdminScreen(vm: QuizViewModel) {
    var exportMessage by rememberSaveable { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(SMALL_SPACE),
    ) {
        ScreenHeader("Администратор", onBack = { vm.back() })
        Button(onClick = { vm.open(Screen.AdminTopics) }, modifier = Modifier.fillMaxWidth()) {
            Text("Банк вопросов")
        }
        Button(onClick = { vm.open(Screen.AdminSettings) }, modifier = Modifier.fillMaxWidth()) {
            Text("Настройки теста")
        }
        Button(onClick = { vm.open(Screen.AdminAttempts) }, modifier = Modifier.fillMaxWidth()) {
            Text("Попытки студентов")
        }
        Button(onClick = { exportMessage = vm.export() }, modifier = Modifier.fillMaxWidth()) {
            Text("Выгрузить всё в JSON")
        }
        exportMessage?.let { Text(it) }
    }
}

/** Банк вопросов, шаг 1: темы. */
@Composable
fun AdminTopicsScreen(vm: QuizViewModel) {
    Column {
        ScreenHeader("Темы", onBack = { vm.back() })
        if (vm.topics.isEmpty()) {
            EmptyMessage("В банке нет вопросов.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(SMALL_SPACE)) {
                items(vm.topics) { topic ->
                    val count = vm.questions.count { it.topic == topic }
                    Card(Modifier.fillMaxWidth().clickable { vm.open(Screen.AdminQuestions(topic)) }) {
                        Column(Modifier.padding(LARGE_SPACE)) {
                            Text(topic, style = MaterialTheme.typography.titleMedium)
                            Text("Вопросов: $count")
                        }
                    }
                }
            }
        }
    }
}

/** Банк вопросов, шаг 2: вопросы одной темы. */
@Composable
fun AdminQuestionsScreen(vm: QuizViewModel, topic: String) {
    val questions = vm.questions.filter { it.topic == topic }.sortedBy { it.level }

    Column {
        ScreenHeader(topic, onBack = { vm.back() })
        if (questions.isEmpty()) {
            EmptyMessage("В этой теме нет вопросов.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(SMALL_SPACE)) {
                items(questions) { question ->
                    Card(Modifier.fillMaxWidth().clickable { vm.open(Screen.AdminQuestion(question)) }) {
                        Column(Modifier.padding(LARGE_SPACE)) {
                            Text("Уровень ${question.level} · ${typeName(question)}")
                            Text(question.text, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
    }
}

/** Банк вопросов, шаг 3: вопрос целиком вместе с правильным ответом. */
@Composable
fun AdminQuestionScreen(vm: QuizViewModel, question: Question) {
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(SMALL_SPACE),
    ) {
        ScreenHeader("Вопрос", onBack = { vm.back() })
        Text("Тема: ${question.topic}")
        Text("Уровень: ${question.level} · тип: ${typeName(question)}")
        Text(question.text, style = MaterialTheme.typography.titleMedium)
        when (question) {
            is MultipleChoiceQuestion -> question.options.forEachIndexed { index, option ->
                Text("${index + 1}. $option")
            }
            is OpenQuestion -> Unit
            is MatchPairQuestion -> Row(horizontalArrangement = Arrangement.spacedBy(LARGE_SPACE)) {
                Column(Modifier.weight(1f)) {
                    question.left.forEachIndexed { index, item -> Text("${index + 1}. $item") }
                }
                Column(Modifier.weight(1f)) {
                    question.right.forEachIndexed { index, item -> Text("${FIRST_LETTER + index}. $item") }
                }
            }
        }
        Text(
            "Правильный ответ: ${question.answerText(question.correctAnswer)}",
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

private fun typeName(question: Question): String = when (question) {
    is MultipleChoiceQuestion -> "тестовый"
    is OpenQuestion -> "открытый"
    is MatchPairQuestion -> "соответствие"
}

/** Параметры теста: лимит времени и лимит вопросов на тему. */
@Composable
fun AdminSettingsScreen(vm: QuizViewModel) {
    var seconds by rememberSaveable { mutableStateOf(vm.settings.timeLimitSeconds.toString()) }
    var maxQuestions by rememberSaveable { mutableStateOf(vm.settings.maxQuestionsPerTopic.toString()) }
    var message by rememberSaveable { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(SMALL_SPACE),
    ) {
        ScreenHeader("Настройки теста", onBack = { vm.back() })
        NumberField(
            value = seconds,
            label = "Время на тест, секунд (${Settings.MIN_TIME_SECONDS}–${Settings.MAX_TIME_SECONDS})",
            onChange = { seconds = it },
        )
        NumberField(
            value = maxQuestions,
            label = "Максимум вопросов на тему (${Settings.MIN_QUESTIONS}–${Settings.MAX_QUESTIONS})",
            onChange = { maxQuestions = it },
        )
        Button(
            onClick = { message = vm.saveSettings(seconds, maxQuestions) },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Сохранить") }
        message?.let { Text(it) }
    }
}

@Composable
private fun NumberField(value: String, label: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
}

/** Все попытки всех студентов. Нажатие — отчет, кнопка под карточкой — удалить. */
@Composable
fun AdminAttemptsScreen(vm: QuizViewModel) {
    val rows = vm.allAttempts().reversed()

    Column {
        ScreenHeader("Попытки студентов", onBack = { vm.back() })
        if (rows.isEmpty()) {
            EmptyMessage("Попыток пока нет.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(SMALL_SPACE)) {
                items(rows) { (login, attempt) ->
                    Column {
                        AttemptCard(attempt, login) {
                            vm.open(Screen.Report(login, attempt, returnTo = Screen.AdminAttempts))
                        }
                        TextButton(onClick = { vm.deleteAttempt(login, attempt) }) {
                            Text("Удалить попытку", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}
