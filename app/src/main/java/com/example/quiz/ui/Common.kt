package com.example.quiz.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.quiz.Attempt

val SMALL_SPACE = 8.dp
val LARGE_SPACE = 16.dp

/** Заголовок экрана с кнопкой «Назад». */
@Composable
fun ScreenHeader(title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = SMALL_SPACE),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SMALL_SPACE),
    ) {
        TextButton(onClick = onBack) { Text("← Назад") }
        Text(title, style = MaterialTheme.typography.titleLarge)
    }
}

/** Понятная надпись вместо пустого экрана. */
@Composable
fun EmptyMessage(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(vertical = LARGE_SPACE),
    )
}

/** Карточка попытки: дата, тема, статус, балл. Нужна и студенту, и администратору. */
@Composable
fun AttemptCard(attempt: Attempt, login: String? = null, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(Modifier.padding(LARGE_SPACE)) {
            login?.let { Text(it, style = MaterialTheme.typography.titleMedium) }
            Text("${attempt.date} · ${attempt.topic}")
            Text("Статус: ${attempt.status} · балл: ${attempt.score} из ${attempt.answers.size}")
        }
    }
}
