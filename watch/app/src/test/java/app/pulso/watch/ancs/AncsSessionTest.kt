package app.pulso.watch.ancs

import app.pulso.watch.ancs.AncsTestBytes.FLAG_PRE_EXISTING
import app.pulso.watch.ancs.AncsTestBytes.buildNotificationSourceBytes
import app.pulso.watch.ancs.AncsTestBytes.buildStandardAttributesResponse
import app.pulso.watch.bluetooth.FakeGattClient
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AncsSessionTest {
  private val gattClient = FakeGattClient(services = setOf(AncsUuids.SERVICE))

  private class StartedSession(val session: AncsSession, val updates: List<AncsUpdate>)

  private suspend fun TestScope.startSession(): StartedSession {
    val session = AncsSession(gattClient, backgroundScope)
    val updates = mutableListOf<AncsUpdate>()
    backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { session.updates.toList(updates) }
    session.start()
    runCurrent()
    return StartedSession(session, updates)
  }

  private suspend fun TestScope.emitEvent(kind: NotificationEventKind, uid: Long, flags: Int = 0) {
    gattClient.emit(AncsUuids.NOTIFICATION_SOURCE, buildNotificationSourceBytes(kind, uid, flags = flags))
    runCurrent()
  }

  private suspend fun TestScope.emitAttributes(uid: Long, title: String, message: String) {
    val response = buildStandardAttributesResponse(uid, "net.whatsapp.WhatsApp", title, message)
    gattClient.emit(AncsUuids.DATA_SOURCE, response)
    runCurrent()
  }

  private fun controlPointWrites() = gattClient.writes.filter { it.first == AncsUuids.CONTROL_POINT }

  @Test
  fun `should enable data source before notification source when session starts`() = runTest {
    startSession()

    assertEquals(listOf(AncsUuids.DATA_SOURCE, AncsUuids.NOTIFICATION_SOURCE), gattClient.enabledNotifications)
  }

  @Test
  fun `should post notification when added event attributes arrive`() = runTest {
    val started = startSession()

    emitEvent(NotificationEventKind.ADDED, uid = 1)
    emitAttributes(uid = 1, title = "Ana", message = "Oi")

    val posted = started.updates.single() as AncsUpdate.NotificationPosted
    assertEquals("Ana", posted.notification.title)
    assertEquals("Oi", posted.notification.message)
  }

  @Test
  fun `should not request attributes when event is pre existing`() = runTest {
    val started = startSession()

    (1L..30L).forEach { uid -> emitEvent(NotificationEventKind.ADDED, uid, flags = FLAG_PRE_EXISTING) }

    assertTrue(controlPointWrites().isEmpty())
    assertTrue(started.updates.isEmpty())
  }

  @Test
  fun `should not post notification when it is removed while attributes are being fetched`() = runTest {
    val started = startSession()

    emitEvent(NotificationEventKind.ADDED, uid = 7)
    emitEvent(NotificationEventKind.REMOVED, uid = 7)
    emitAttributes(uid = 7, title = "Ana", message = "apagada")

    assertEquals(listOf(AncsUpdate.NotificationRemoved(7)), started.updates)
  }

  @Test
  fun `should fetch attributes one at a time when two notifications arrive together`() = runTest {
    val started = startSession()

    emitEvent(NotificationEventKind.ADDED, uid = 1)
    emitEvent(NotificationEventKind.ADDED, uid = 2)
    assertEquals(1, controlPointWrites().size)

    emitAttributes(uid = 1, title = "Ana", message = "primeira")
    assertEquals(2, controlPointWrites().size)
    emitAttributes(uid = 2, title = "Bia", message = "segunda")

    val messages = started.updates.map { (it as AncsUpdate.NotificationPosted).notification.message }
    assertEquals(listOf("primeira", "segunda"), messages)
  }

  @Test
  fun `should emit changed when modified event attributes arrive`() = runTest {
    val started = startSession()

    emitEvent(NotificationEventKind.MODIFIED, uid = 4)
    emitAttributes(uid = 4, title = "Ana", message = "editada")

    val changed = started.updates.single() as AncsUpdate.NotificationChanged
    assertEquals("editada", changed.notification.message)
  }

  @Test
  fun `should write negative action command when performing dismiss`() = runTest {
    val started = startSession()

    started.session.performAction(9, NotificationAction.NEGATIVE)

    val expected = ControlPointCommands.buildPerformNotificationAction(9, NotificationAction.NEGATIVE)
    assertArrayEquals(expected, controlPointWrites().last().second)
  }

  @Test
  fun `should skip notification and continue when attributes time out`() = runTest {
    val started = startSession()

    emitEvent(NotificationEventKind.ADDED, uid = 1)
    emitEvent(NotificationEventKind.ADDED, uid = 2)
    advanceTimeBy(5_001)
    runCurrent()
    emitAttributes(uid = 2, title = "Bia", message = "depois do timeout")

    val posted = started.updates.single() as AncsUpdate.NotificationPosted
    assertEquals(2L, posted.notification.uid)
  }
}
