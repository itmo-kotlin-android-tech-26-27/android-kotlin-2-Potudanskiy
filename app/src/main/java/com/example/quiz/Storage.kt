package com.example.quiz

import android.content.Context
import android.util.Log
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.put
import java.io.File
import java.util.Date

private const val QUESTIONS_FILE = "questions.json"
private const val STUDENTS_FILE = "students.json"
private const val SETTINGS_FILE = "settings.json"
private const val DEMO_FILE = "demo-questions.json"
private const val EXPORT_FILE = "export.json"
private const val BROKEN_SUFFIX = ".broken"
private const val LOG_TAG = "Storage"

/** Чтение и запись JSON-файлов во внутренней папке приложения. */
class Storage(private val context: Context) {
    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
    }
    private val folder = context.filesDir

    /** Сообщение о поврежденных файлах, если такие нашлись при загрузке. */
    var loadError: String? = null
        private set

    fun loadQuestions(): List<Question> {
        val saved = load<List<Question>>(QUESTIONS_FILE)
        if (saved != null) return saved
        val demoText = context.assets.open(DEMO_FILE).bufferedReader().use { it.readText() }
        val demo = json.decodeFromString<List<Question>>(demoText)
        save(QUESTIONS_FILE, demo)
        return demo
    }

    fun loadStudents(): List<Student> = load<List<Student>>(STUDENTS_FILE) ?: emptyList()

    fun loadSettings(): Settings = load<Settings>(SETTINGS_FILE) ?: Settings()

    fun saveStudents(students: List<Student>) = save(STUDENTS_FILE, students)

    fun saveSettings(settings: Settings) = save(SETTINGS_FILE, settings)

    /** Один файл со всем для анализа: настройки, банк вопросов, студенты и их попытки. Возвращает путь. */
    fun export(questions: List<Question>, students: List<Student>, settings: Settings): String {
        val data = buildJsonObject {
            put("exportedAt", Date().toString())
            put("settings", json.encodeToJsonElement(settings))
            put("questions", json.encodeToJsonElement(questions))
            put("students", json.encodeToJsonElement(students))
        }
        val file = File(context.getExternalFilesDir(null) ?: folder, EXPORT_FILE)
        file.writeText(json.encodeToString(data))
        return file.absolutePath
    }

    /** Нет файла — null. Файл поврежден — откладываем его в сторону и тоже возвращаем null. */
    private inline fun <reified T> load(fileName: String): T? {
        val file = File(folder, fileName)
        if (!file.exists()) return null
        return try {
            json.decodeFromString<T>(file.readText())
        } catch (e: IllegalArgumentException) {
            putAside(file, e)
            null
        }
    }

    private inline fun <reified T> save(fileName: String, value: T) {
        File(folder, fileName).writeText(json.encodeToString(value))
    }

    private fun putAside(file: File, error: IllegalArgumentException) {
        Log.w(LOG_TAG, "Поврежден файл ${file.name}", error)
        val brokenName = file.name + BROKEN_SUFFIX
        file.renameTo(File(folder, brokenName))
        val message = "Файл ${file.name} поврежден и сохранен как $brokenName. Загружены данные по умолчанию."
        loadError = listOfNotNull(loadError, message).joinToString("\n")
    }
}
