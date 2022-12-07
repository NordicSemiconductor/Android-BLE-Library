package no.nordicsemi.android.ble.trivia.server.model

import no.nordicsemi.android.ble.trivia.server.repository.Answer
import no.nordicsemi.android.ble.trivia.server.data.ColorState
import no.nordicsemi.android.ble.trivia.server.data.DisplayAnswer

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