package com.example.quiz.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.quiz.QuizViewModel
import com.example.quiz.Screen

/** Вход: студент вводит логин, администратор входит без логина. */
@Composable
fun LoginScreen(vm: QuizViewModel) {
    // rememberSaveable: текст в поле переживет поворот экрана.
    var login by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(SMALL_SPACE),
    ) {
        Text("Тест-экзаменатор", style = MaterialTheme.typography.headlineMedium)
        vm.loadError?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        Text(
            "Студент",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = LARGE_SPACE),
        )
        OutlinedTextField(
            value = login,
            onValueChange = {
                login = it
                error = null
            },
            label = { Text("Логин") },
            singleLine = true,
            isError = error != null,
            supportingText = { error?.let { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
        )
        Button(
            onClick = { if (login.isBlank()) error = "Введите логин" else vm.loginStudent(login) },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Войти как студент") }

        Text(
            "Администратор",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = LARGE_SPACE),
        )
        OutlinedButton(
            onClick = { vm.open(Screen.Admin) },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Войти как администратор") }
    }
}
