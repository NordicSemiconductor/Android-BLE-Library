package no.nordicsemi.android.ble.example.game.server.data

import no.nordicsemi.android.ble.example.game.server.repository.Question

sealed interface GameState

data class WaitingForPlayers(val connectedPlayers: Int): GameState

object DownloadingQuestions: GameState

data class Round(
    val question: Question
): GameState
