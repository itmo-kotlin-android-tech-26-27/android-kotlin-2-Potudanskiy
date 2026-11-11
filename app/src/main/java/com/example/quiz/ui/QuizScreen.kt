package com.example.quiz.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.quiz.Attempt
import com.example.quiz.FIRST_LETTER
import com.example.quiz.MatchPairQuestion
import com.example.quiz.MultipleChoiceQuestion
import com.example.quiz.OpenQuestion
import com.example.quiz.Question
import com.example.quiz.QuizViewModel
import com.example.quiz.TestSession

private const val SECONDS_IN_MINUTE = 60
private const val WARNING_SECONDS = 10
private const val EMPTY_SLOT = '\u0000'

/** Весь тест идет на одном экране: меняется только вопрос. */
@Composable
fun QuizScreen(vm: QuizViewModel) {
    val session = vm.session ?: return
    val question = session.current ?: return
    // Ключ — номер вопроса: на новом вопросе черновик и ошибка сбрасываются, а при повороте сохраняются.
    var draft by rememberSaveable(session.number) { mutableStateOf("") }
    var error by rememberSaveable(session.number) { mutableStateOf<String?>(null) }
    val canAnswer = !session.isAnswered

    Column(Modifier.fillMaxSize()) {
        QuizHeader(session, question, vm.secondsLeft)
        HorizontalDivider(Modifier.padding(vertical = SMALL_SPACE))

        // weight(1f) — середина занимает все свободное место и прокручивается.
        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(SMALL_SPACE),
        ) {
            Text(question.text, style = MaterialTheme.typography.bodyLarge)
            when (question) {
                is MultipleChoiceQuestion -> ChoiceAnswer(question, draft, canAnswer) { draft = it }
                is OpenQuestion -> OpenAnswer(draft, canAnswer) { draft = it }
                is MatchPairQuestion -> MatchAnswer(question, draft, canAnswer) { draft = it }
            }
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            if (!canAnswer) Text("Ответ принят. Нажмите «Далее».")
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = SMALL_SPACE),
            horizontalArrangement = Arrangement.spacedBy(SMALL_SPACE),
        ) {
            Button(
                onClick = {
                    error = if (question.isValidAnswer(draft)) null else answerHint(question)
                    if (error == null) vm.answer(draft)
                },
                enabled = canAnswer,
                modifier = Modifier.weight(1f),
            ) { Text("Ответить") }
            Button(
                onClick = { vm.next() },
                enabled = !canAnswer,
                modifier = Modifier.weight(1f),
            ) { Text("Далее") }
        }
        OutlinedButton(
            onClick = { vm.finishTest(Attempt.INTERRUPTED) },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Прервать тест") }
    }
}

/** Шапка: тема, номер вопроса, ступень и оставшееся время. */
@Composable
private fun QuizHeader(session: TestSession, question: Question, secondsLeft: Int) {
    val timeColor = if (secondsLeft <= WARNING_SECONDS) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.primary
    }
    Column {
        Text(session.topic, style = MaterialTheme.typography.titleLarge)
        Text("Вопрос ${session.number} · ступень ${question.level} из ${session.maxLevel}")
        Text(
            "Осталось: %d:%02d".format(secondsLeft / SECONDS_IN_MINUTE, secondsLeft % SECONDS_IN_MINUTE),
            style = MaterialTheme.typography.titleMedium,
            color = timeColor,
        )
    }
}

/** Тестовый вопрос: радиокнопки, выбрать можно один вариант. Ответ — номер варианта. */
@Composable
private fun ChoiceAnswer(
    question: MultipleChoiceQuestion,
    selected: String,
    enabled: Boolean,
    onSelect: (String) -> Unit,
) {
    question.options.forEachIndexed { index, option ->
        val number = (index + 1).toString()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .selectable(selected = selected == number, enabled = enabled, onClick = { onSelect(number) }),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RadioButton(selected = selected == number, onClick = null, enabled = enabled)
            Text(option, modifier = Modifier.padding(start = SMALL_SPACE))
        }
    }
}

@Composable
private fun OpenAnswer(text: String, enabled: Boolean, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = text,
        onValueChange = onChange,
        label = { Text("Ваш ответ") },
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
    )
}

/**
 * Соответствие: два столбца — слева номера, справа буквы.
 * Под ними для каждого номера выбирается буква. Ответ — строка букв, например "BCA".
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MatchAnswer(
    question: MatchPairQuestion,
    letters: String,
    enabled: Boolean,
    onChange: (String) -> Unit,
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(LARGE_SPACE)) {
        Column(Modifier.weight(1f)) {
            question.left.forEachIndexed { index, item -> Text("${index + 1}. $item") }
        }
        Column(Modifier.weight(1f)) {
            question.right.forEachIndexed { index, item -> Text("${FIRST_LETTER + index}. $item") }
        }
    }
    Text(
        "Выберите пару для каждого номера. Повторное нажатие снимает выбор.",
        style = MaterialTheme.typography.titleSmall,
    )
    question.left.indices.forEach { leftIndex ->
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("${leftIndex + 1} →", modifier = Modifier.padding(end = SMALL_SPACE))
            FlowRow(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(SMALL_SPACE)) {
                question.right.indices.forEach { rightIndex ->
                    val letter = FIRST_LETTER + rightIndex
                    val selected = letters.getOrNull(leftIndex) == letter
                    FilterChip(
                        selected = selected,
                        onClick = {
                            val newLetter = if (selected) EMPTY_SLOT else letter
                            onChange(withLetter(letters, leftIndex, newLetter, question.left.size))
                        },
                        label = { Text(letter.toString()) },
                        enabled = enabled && (selected || letter !in letters),
                    )
                }
            }
        }
    }
}

/** Меняет букву в одной строке. Невыбранные места — EMPTY_SLOT. */
private fun withLetter(letters: String, index: Int, letter: Char, size: Int): String =
    letters.padEnd(size, EMPTY_SLOT).toCharArray().also { it[index] = letter }.concatToString()

private fun answerHint(question: Question): String = when (question) {
    is MultipleChoiceQuestion -> "Выберите вариант ответа."
    is OpenQuestion -> "Введите ответ."
    is MatchPairQuestion -> "Выберите букву для каждого номера, буквы не должны повторяться."
}
