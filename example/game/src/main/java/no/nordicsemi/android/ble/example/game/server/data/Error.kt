package no.nordicsemi.android.ble.example.game.server.data

data class Error(
    val isEmptyName: Boolean,
    val isDuplicateName: Boolean,
) {
    fun isError() = isDuplicateName || isEmptyName
}

