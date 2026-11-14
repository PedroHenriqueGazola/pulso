package app.pulso.watch.calls

import app.pulso.watch.ancs.AncsCategory
import app.pulso.watch.ancs.IphoneNotification
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CallClassifierTest {
  private fun notification(
    appIdentifier: String = "net.whatsapp.WhatsApp",
    category: AncsCategory = AncsCategory.SOCIAL,
    title: String = "",
    message: String = "",
  ) = IphoneNotification(
    uid = 1,
    appIdentifier = appIdentifier,
    title = title,
    subtitle = "",
    message = message,
    category = category,
    isSilent = false,
    hasPositiveAction = false,
    hasNegativeAction = false,
    positiveActionLabel = "",
    negativeActionLabel = "",
  )

  @Test
  fun `should classify as call when category is incoming call`() {
    val call = notification(appIdentifier = "com.example.any", category = AncsCategory.INCOMING_CALL)

    assertTrue(CallClassifier.isIncomingCall(call))
  }

  @Test
  fun `should classify as call when whatsapp message says chamada de vídeo with accent`() {
    assertTrue(CallClassifier.isIncomingCall(notification(title = "Ana", message = "Chamada de vídeo do WhatsApp")))
  }

  @Test
  fun `should classify as call when whatsapp message says chamada de video without accent`() {
    assertTrue(CallClassifier.isIncomingCall(notification(title = "Ana", message = "chamada de video")))
  }

  @Test
  fun `should classify as call when facetime title says is calling`() {
    val call = notification(appIdentifier = "com.apple.facetime", title = "Ana is calling")

    assertTrue(CallClassifier.isIncomingCall(call))
  }

  @Test
  fun `should not classify as call when whatsapp text message mentions nothing about calls`() {
    assertFalse(CallClassifier.isIncomingCall(notification(title = "Ana", message = "Bora almoçar?")))
  }

  @Test
  fun `should not classify as call when non call app says chamada de voz`() {
    val email = notification(appIdentifier = "com.google.Gmail", category = AncsCategory.EMAIL, message = "Chamada de voz")

    assertFalse(CallClassifier.isIncomingCall(email))
  }
}
