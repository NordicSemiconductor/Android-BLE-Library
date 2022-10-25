package no.nordicsemi.android.ble.example.game.client.view

import no.nordicsemi.android.ble.example.game.proto.ResultProto

data class Result(
    val deviceName: String,
    val selectedAnswerId: Int
)

fun Result.toProto() = ResultProto(deviceName, selectedAnswerId)

fun ResultProto.toResult() = Result(name, selectedAnswerId)