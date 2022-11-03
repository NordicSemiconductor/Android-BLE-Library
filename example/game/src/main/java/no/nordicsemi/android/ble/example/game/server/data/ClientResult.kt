package no.nordicsemi.android.ble.example.game.server.data

import no.nordicsemi.android.ble.example.game.proto.ResultProto

/**
 * Result to be received from remote players.
 * @property playersName        name of the player.
 * @property selectedAnswerId   selected answer id.
 * */
data class ClientResult(
    val playersName: String,
    val selectedAnswerId: Int
)

fun ClientResult.toProto() = ResultProto(playersName, selectedAnswerId)

fun ResultProto.toResult() = ClientResult(name, selectedAnswerId)