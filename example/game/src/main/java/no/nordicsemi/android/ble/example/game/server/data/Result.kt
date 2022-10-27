package no.nordicsemi.android.ble.example.game.server.data

import no.nordicsemi.android.ble.example.game.proto.FinalResultProto
import no.nordicsemi.android.ble.example.game.proto.ResultToClientProto


/**
 * Final result to send to all players.
 * @property isGameOver Will be true once game is over.
 * @property finalResult Contains a list of players with score.
 *
 * */
data class ResultToClient(
    val isGameOver: Boolean,
    val finalResult: List<FinalResult>
)

data class FinalResult(
    val name : String,
    var score: Int,
)


fun ResultToClient.toProto() = ResultToClientProto(isGameOver, finalResult.map { it.toProto() })

fun FinalResult.toProto() = FinalResultProto(name, score)

fun ResultToClientProto.toResultToClient() = ResultToClient(isGameOver, finalResult.map { it.finalResult() })

fun FinalResultProto.finalResult() = FinalResult(name, score)


