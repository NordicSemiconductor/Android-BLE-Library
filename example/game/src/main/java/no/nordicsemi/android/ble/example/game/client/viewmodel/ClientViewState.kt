package no.nordicsemi.android.ble.example.game.client.viewmodel

import no.nordicsemi.android.ble.example.game.quiz.model.toViewState
import no.nordicsemi.android.ble.example.game.quiz.repository.Question
import no.nordicsemi.android.ble.example.game.server.viewmodel.DisplayAnswer
import no.nordicsemi.android.ble.example.game.server.data.Players
import no.nordicsemi.android.ble.example.game.server.data.Results
import no.nordicsemi.android.ble.ktx.state.ConnectionState

data class ClientViewState(
    val state: ConnectionState = ConnectionState.Initializing,
    val correctAnswerId: Int? = null,
    val selectedAnswerId: Int? = null,
    val ticks: Long? = null,
    val question: Question? = null,
    val userJoined: Players? = null,
    val isGameOver: Boolean? = null,
    val result: Results? = null
) {
    val isTimerRunning: Boolean = ticks?.let { it > 0 } == true
}

fun ClientViewState.toViewState(): List<DisplayAnswer> {
    return question?.let { question ->
        question.answers.map { it.toViewState(selectedAnswerId, correctAnswerId, isTimerRunning) }
    } ?: emptyList()
}