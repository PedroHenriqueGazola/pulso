package app.pulso.watch.service

import app.pulso.watch.ancs.AncsCategory
import app.pulso.watch.ancs.AncsUpdate
import app.pulso.watch.ancs.IphoneNotification
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IphoneUpdateRouterTest {
  private class RecordingNotificationDisplay : NotificationDisplay {
    val events = mutableListOf<String>()
    override fun show(notification: IphoneNotification) { events += "show:${notification.uid}" }
    override fun remove(uid: Long) { events += "remove:$uid" }
  }

  private class RecordingCallHandler : CallHandler {
    val events = mutableListOf<String>()
    override fun onCallPosted(notification: IphoneNotification) { events += "posted:${notification.uid}" }
    override fun onCallChanged(notification: IphoneNotification) { events += "changed:${notification.uid}" }
    override fun onRemoved(uid: Long) { events += "removed:$uid" }
  }

  private val display = RecordingNotificationDisplay()
  private val calls = RecordingCallHandler()
  private val router = IphoneUpdateRouter(display, calls)

  private fun notification(uid: Long, category: AncsCategory) = IphoneNotification(
    uid = uid,
    appIdentifier = "com.apple.mobilephone",
    title = "Ana",
    subtitle = "",
    message = "",
    category = category,
    isSilent = false,
    hasPositiveAction = true,
    hasNegativeAction = true,
    positiveActionLabel = "",
    negativeActionLabel = "",
  )

  @Test
  fun `should show notification when posted update is a regular message`() {
    router.route(AncsUpdate.NotificationPosted(notification(1, AncsCategory.SOCIAL)))

    assertEquals(listOf("show:1"), display.events)
    assertTrue(calls.events.isEmpty())
  }

  @Test
  fun `should send call to call handler when posted update is an incoming call`() {
    router.route(AncsUpdate.NotificationPosted(notification(2, AncsCategory.INCOMING_CALL)))

    assertEquals(listOf("posted:2"), calls.events)
    assertTrue(display.events.isEmpty())
  }

  @Test
  fun `should send change to call handler when changed update is an incoming call`() {
    router.route(AncsUpdate.NotificationChanged(notification(2, AncsCategory.INCOMING_CALL)))

    assertEquals(listOf("changed:2"), calls.events)
  }

  @Test
  fun `should update notification when changed update is a regular message`() {
    router.route(AncsUpdate.NotificationChanged(notification(3, AncsCategory.EMAIL)))

    assertEquals(listOf("show:3"), display.events)
  }

  @Test
  fun `should remove from display and calls when notification is removed`() {
    router.route(AncsUpdate.NotificationRemoved(4))

    assertEquals(listOf("remove:4"), display.events)
    assertEquals(listOf("removed:4"), calls.events)
  }
}
