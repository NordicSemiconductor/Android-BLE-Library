package no.nordicsemi.android.ble.example.game.server.data

import no.nordicsemi.android.ble.example.game.proto.ResultProto
import no.nordicsemi.android.ble.example.game.proto.ResultsProto

/**
 * Final result to send to all players.
 * @property isGameOver will be true once game is over.
 * @property result     contains a list of players with score.
 * */
data class Results(
    val isGameOver: Boolean,
    val result: List<Result>
)

data class Result(
    val name: String,
    var score: Int,
)

fun Results.toProto() = ResultsProto(isGameOver, result.map { it.toProto() })

fun Result.toProto() = ResultProto(name, score)

fun ResultsProto.toResults() = Results(isGameOver, result.map { it.result() })

fun ResultProto.result() = Result(name, score)


