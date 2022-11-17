package no.nordicsemi.android.ble.example.game.server.viewmodel

import no.nordicsemi.android.ble.example.game.quiz.model.toViewState
import no.nordicsemi.android.ble.example.game.server.data.Player
import no.nordicsemi.android.ble.example.game.server.data.Result

data class ServerViewState(
    val state: GameState = WaitingForPlayers(0),
    val correctAnswerId: Int? = null,
    val selectedAnswerId: Int? = null,
    val ticks: Long? = null,
    val userJoined: List<Player> = emptyList(),
    val isGameOver: Boolean? = null,
    val result: List<Result> = emptyList(),
) {
    val isTimerRunning: Boolean = ticks?.let { it > 0 } == true
}

fun ServerViewState.toViewState(): List<DisplayAnswer> {
    return when (state) {
        is Round -> state.question.answers
            .map { it.toViewState(selectedAnswerId, correctAnswerId, isTimerRunning) }
        else -> emptyList()
    }
}