package no.nordicsemi.android.ble.example.game.quiz.view

fun String.replace(): String {
    val mapSpecialCharacter = mapOf(
        "&quot;" to "'",
        "&#039;" to "'",
        "&ldquo;" to "'",
        "&ouml;" to "ö",
        "&uuml;" to "ü",
        "&Uuml;" to "Ü",
        "&auml;" to "ä",
        "&amp;" to "&",
        "&micro;" to "µ",
        "&ntilde;" to "ñ",
    )
    var replacedText = this
    mapSpecialCharacter.forEach { (oldName, newName) ->
        replacedText =
            replacedText.replace(oldName, newName)
    }
    return replacedText
}