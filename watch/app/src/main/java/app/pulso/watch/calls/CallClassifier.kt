package app.pulso.watch.calls

import app.pulso.watch.ancs.AncsCategory
import app.pulso.watch.ancs.IphoneNotification
import java.text.Normalizer

object CallClassifier {
  private val COMBINING_MARKS = Regex("\\p{Mn}+")

  fun isIncomingCall(notification: IphoneNotification): Boolean {
    if (notification.category == AncsCategory.INCOMING_CALL) return true
    if (notification.appIdentifier !in CALL_APP_IDENTIFIERS) return false

    val searchableText = normalize("${notification.title} ${notification.subtitle} ${notification.message}")
    return CALL_TEXT_PATTERNS.any { pattern -> searchableText.contains(pattern) }
  }

  private fun normalize(text: String): String =
    Normalizer.normalize(text.lowercase(), Normalizer.Form.NFD).replace(COMBINING_MARKS, "")
}
