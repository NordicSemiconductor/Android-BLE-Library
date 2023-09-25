package no.nordicsemi.android.ble.trivia.server.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

object Timer {
    const val TOTAL_TIME = 20_000L
}

open class TimerViewModel(
    @ApplicationContext application: Context
): AndroidViewModel(application = application as Application) {

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
