package no.nordicsemi.android.ble.example.game.server.viewmodel

import no.nordicsemi.android.ble.example.game.quiz.repository.Question

sealed class GameState

data class WaitingForPlayers(val connectedPlayers: Int): GameState()

object DownloadingQuestions: GameState()

data class Round(
    val question: Question
): GameState()