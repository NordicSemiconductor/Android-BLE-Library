package no.nordicsemi.android.ble.trivia.server.data

data class NameResult(
    val isEmptyName: Boolean,
    val isDuplicateName: Boolean,
) {
    fun isInvalid() = isDuplicateName || isEmptyName
}

