package com.example.quiz

import kotlinx.serialization.Serializable

@Serializable
data class Student(
    val login: String,
    val attempts: List<Attempt> = emptyList(),
) {
    init {
        require(login.isNotBlank()) { "Пустой логин студента." }
    }
}
