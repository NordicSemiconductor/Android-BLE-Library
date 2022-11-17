package no.nordicsemi.android.ble.example.game.server.model

import android.os.Build
import android.text.Html

/**
 * A mapper method to map a Html tag/number into a string.
 */
fun htmlTextMapper(html: String?): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY).toString()
    } else {
        @Suppress("DEPRECATION")
        Html.fromHtml(html).toString()
    }
}