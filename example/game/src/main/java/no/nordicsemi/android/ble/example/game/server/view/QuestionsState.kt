package no.nordicsemi.android.ble.example.game.quiz.view

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import no.nordicsemi.android.ble.example.game.quiz.repository.Question

data class Questions(
    val questionsState: List<QuestionState>
) {

    var currentQuestionIndex by mutableStateOf(0)
}

class QuestionState(
    val question: Question,
    val questionIndex: Int,
    val totalQuestions: Int,
    val showDone: Boolean
) {
    var enableNext by mutableStateOf(false)
    var givenAnswerId by mutableStateOf(0)
}

