package no.nordicsemi.android.ble.example.game.timer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

open class TimerViewModel: ViewModel() {
    val ticksTotal = 20_000L

    private val _ticks = MutableStateFlow(ticksTotal)
    val ticks = _ticks.asStateFlow()

    val timerFinished = _ticks.transform { value ->
        if (value == 0L) {
            emit(Unit)
        }
    }

    fun startCountDown() {
        viewModelScope.launch {
            while (_ticks.value > 0) {
                delay(1.seconds)
                _ticks.value -= 1_000
            }
        }
    }
}
