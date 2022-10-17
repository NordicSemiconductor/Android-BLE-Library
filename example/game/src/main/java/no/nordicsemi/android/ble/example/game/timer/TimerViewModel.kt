package no.nordicsemi.android.ble.example.game.timer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class TimerViewModel @Inject constructor() : ViewModel() {

    private val _timerState = MutableStateFlow(false)
    val timerState = _timerState.asStateFlow()

    private val _ticks = MutableStateFlow<Long>(20_000)
    val ticks = _ticks.asStateFlow()

    private val _progress = MutableStateFlow<Float>(1f)
    val progress = _progress.asStateFlow()

    fun startCountDown() {
        viewModelScope.launch {
            while (_ticks.value > 0) {
                _progress.value -= .05f
                _timerState.value = true
                delay(1.seconds)
                _ticks.value -= 1_000
            }
            _progress.value = 1f
            _timerState.value = false
        }
    }
}
