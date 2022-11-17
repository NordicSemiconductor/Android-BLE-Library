package no.nordicsemi.android.ble.example.game.client.data

import no.nordicsemi.android.ble.example.game.server.model.toViewState
import no.nordicsemi.android.ble.example.game.server.repository.Question
import no.nordicsemi.android.ble.example.game.server.data.DisplayAnswer
import no.nordicsemi.android.ble.example.game.server.data.Players
import no.nordicsemi.android.ble.example.game.server.data.Results
import no.nordicsemi.android.ble.ktx.state.ConnectionState

/**
 * It holds the data to be used in the client screen.
 * @property state              connection state.
 * @property correctAnswerId    correct answer id.
 * @property selectedAnswerId   selected answer id.
 * @property ticks              timer duration.
 * @property question           question sent from the server.
 * @property userJoined         a list of all joined players.
 * @property isGameOver         returns true when game is over.
 * @property result             a list of players and their scores.
 * @property isTimerRunning     returns true when timer is running.
 */

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