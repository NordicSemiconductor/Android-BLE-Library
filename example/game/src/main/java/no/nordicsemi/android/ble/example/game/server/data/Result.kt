package no.nordicsemi.android.ble.example.game.server.data

import no.nordicsemi.android.ble.example.game.proto.FinalResultProto
import no.nordicsemi.android.ble.example.game.proto.ResultToClientProto

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

fun Results.toProto() = ResultToClientProto(isGameOver, result.map { it.toProto() })

fun Result.toProto() = FinalResultProto(name, score)

fun ResultToClientProto.toResultToClient() =
    Results(isGameOver, finalResult.map { it.finalResult() })

fun FinalResultProto.finalResult() = Result(name, score)


