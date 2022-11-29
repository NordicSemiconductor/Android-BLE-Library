package no.nordicsemi.android.ble.trivia.server.data

import no.nordicsemi.android.ble.trivia.proto.PlayerProto
import no.nordicsemi.android.ble.trivia.proto.PlayersProto

/**
 * A list of player's name.
 */
data class Players(
    val player: List<Player>
)

data class Player(
    val name: String
)

fun Players.toProto() = PlayersProto(player.map { it.toProto() })

fun Player.toProto() = PlayerProto(name)

fun PlayersProto.toPlayers() = Players(player.map { it.player() })

fun PlayerProto.player( )= Player(name)