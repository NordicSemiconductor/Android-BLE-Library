package no.nordicsemi.android.ble.example.game.server.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

object Timer {
    const val TOTAL_TIME = 20_000L
}

open class TimerViewModel: ViewModel() {

    private val _ticks = MutableStateFlow(Timer.TOTAL_TIME)
    val ticks = _ticks.asStateFlow()

    val timerFinished = _ticks.transform { value ->
        if (value == 0L) {
            emit(Unit)
        }
    }

    fun startCountDown() {
        viewModelScope.launch {
            _ticks.value = Timer.TOTAL_TIME

            while (_ticks.value > 0) {
                delay(1.seconds)
                _ticks.value -= 1_000
            }
        }
    }
}
