package no.nordicsemi.android.ble.trivia.server.data

data class Error(
    val isEmptyName: Boolean,
    val isDuplicateName: Boolean,
) {
    fun isError() = isDuplicateName || isEmptyName
}

