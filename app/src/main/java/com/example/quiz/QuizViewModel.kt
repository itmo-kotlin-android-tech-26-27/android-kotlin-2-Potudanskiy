package com.example.quiz

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException

private const val ONE_SECOND_MS = 1000L

/**
 * Состояние и логика всего приложения. ViewModel переживает поворот экрана,
 * поэтому тест и таймер не сбрасываются. Поля с mutableStateOf — это состояние:
 * когда они меняются, Compose сам перерисовывает экраны, которые их читают.
 */
class QuizViewModel(application: Application) : AndroidViewModel(application) {
    private val storage = Storage(application)

    val questions: List<Question> = storage.loadQuestions()
    val topics: List<String> = questions.map { it.topic }.distinct()
    var students by mutableStateOf(storage.loadStudents())
        private set
    var settings by mutableStateOf(storage.loadSettings())
        private set
    val loadError: String? = storage.loadError

    var screen by mutableStateOf<Screen>(Screen.Login)
        private set
    var login by mutableStateOf<String?>(null)
        private set
    var session by mutableStateOf<TestSession?>(null)
        private set
    var secondsLeft by mutableIntStateOf(0)
        private set

    private var timer: Job? = null

    fun open(target: Screen) {
        screen = target
    }

    fun loginStudent(name: String) {
        val trimmed = name.trim()
        if (students.none { it.login == trimmed }) {
            students = students + Student(trimmed)
            storage.saveStudents(students)
        }
        login = trimmed
        screen = Screen.Topics
    }

    fun logout() {
        login = null
        screen = Screen.Login
    }

    fun attemptsOf(login: String): List<Attempt> =
        students.firstOrNull { it.login == login }?.attempts.orEmpty()

    fun allAttempts(): List<Pair<String, Attempt>> =
        students.flatMap { student -> student.attempts.map { student.login to it } }

    // ---------- Тест ----------

    fun startTest(topic: String) {
        val topicQuestions = questions.filter { it.topic == topic }
        val newSession = TestSession(topic, topicQuestions, settings.maxQuestionsPerTopic).next()
        if (newSession.current == null) return
        session = newSession
        screen = Screen.Quiz
        startTimer()
    }

    fun answer(given: String) {
        session = session?.answer(given)
    }

    fun next() {
        val nextSession = session?.next() ?: return
        session = nextSession
        if (nextSession.current == null) finishTest(Attempt.COMPLETED)
    }

    /** Сохраняет попытку и открывает отчет. Уход с экрана теста — статус «прервана». */
    fun finishTest(status: String) {
        val currentSession = session ?: return
        val currentLogin = login ?: return
        timer?.cancel()
        val attempt = currentSession.toAttempt(status)
        session = null
        changeAttempts(currentLogin) { it + attempt }
        screen = Screen.Report(currentLogin, attempt)
    }

    /** Таймер — корутина: раз в секунду уменьшает счетчик, на нуле завершает тест. */
    private fun startTimer() {
        timer?.cancel()
        secondsLeft = settings.timeLimitSeconds
        timer = viewModelScope.launch {
            while (secondsLeft > 0) {
                delay(ONE_SECOND_MS)
                secondsLeft--
            }
            finishTest(Attempt.TIME_OUT)
        }
    }

    /** Приложение закрыли посреди теста — попытка тоже сохраняется как прерванная. */
    override fun onCleared() {
        finishTest(Attempt.INTERRUPTED)
    }

    // ---------- Администратор ----------

    fun deleteAttempt(login: String, attempt: Attempt) = changeAttempts(login) { it - attempt }

    /** Возвращает сообщение для экрана настроек. */
    fun saveSettings(seconds: String, maxQuestions: String): String {
        val time = seconds.trim().toIntOrNull()
        val count = maxQuestions.trim().toIntOrNull()
        if (time == null || count == null) return "Введите целые числа."
        val newSettings = try {
            Settings(time, count)
        } catch (e: IllegalArgumentException) {
            return e.message ?: "Неверные значения."
        }
        settings = newSettings
        storage.saveSettings(newSettings)
        return "Настройки сохранены."
    }

    fun export(): String = try {
        "Файл создан:\n${storage.export(questions, students, settings)}"
    } catch (e: IOException) {
        "Не удалось создать файл: ${e.message}"
    }

    private fun changeAttempts(login: String, change: (List<Attempt>) -> List<Attempt>) {
        students = students.map { if (it.login == login) it.copy(attempts = change(it.attempts)) else it }
        storage.saveStudents(students)
    }

    // ---------- Кнопка «Назад» ----------

    fun back() {
        when (val current = screen) {
            Screen.Login -> Unit
            Screen.Topics, Screen.Admin -> logout()
            Screen.Quiz -> finishTest(Attempt.INTERRUPTED)
            Screen.History -> screen = Screen.Topics
            is Screen.Report -> screen = if (current.fromAdmin) Screen.AdminAttempts else Screen.Topics
            Screen.AdminTopics, Screen.AdminSettings, Screen.AdminAttempts -> screen = Screen.Admin
            is Screen.AdminQuestions -> screen = Screen.AdminTopics
            is Screen.AdminQuestion -> screen = Screen.AdminQuestions(current.question.topic)
        }
    }
}
