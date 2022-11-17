package no.nordicsemi.android.ble.example.game.quiz.model

import no.nordicsemi.android.ble.example.game.quiz.repository.Answer
import no.nordicsemi.android.ble.example.game.server.viewmodel.ColorState
import no.nordicsemi.android.ble.example.game.server.viewmodel.DisplayAnswer

fun Answer.toViewState(
    selectedAnswerId: Int? = null,
    correctAnswerId: Int? = null,
    isTimerRunning: Boolean,
): DisplayAnswer =
    DisplayAnswer(
        id,
        text,
        isSelected = id == selectedAnswerId,
        enableSelection = isTimerRunning && selectedAnswerId == null,
        color = when {
            id == correctAnswerId -> ColorState.CORRECT
            id == selectedAnswerId && isTimerRunning -> ColorState.SELECTED_AND_TIMER_RUNNING
            isTimerRunning || id != selectedAnswerId -> ColorState.NOT_SELECTED_AND_TIMER_RUNNING
            else -> ColorState.NONE
        }
    )