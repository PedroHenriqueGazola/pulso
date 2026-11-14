package app.pulso.watch.ancs

import app.pulso.watch.ancs.AncsTestBytes.FLAG_IMPORTANT
import app.pulso.watch.ancs.AncsTestBytes.FLAG_NEGATIVE_ACTION
import app.pulso.watch.ancs.AncsTestBytes.FLAG_POSITIVE_ACTION
import app.pulso.watch.ancs.AncsTestBytes.FLAG_PRE_EXISTING
import app.pulso.watch.ancs.AncsTestBytes.FLAG_SILENT
import app.pulso.watch.ancs.AncsTestBytes.buildNotificationSourceBytes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationSourceParserTest {
  @Test
  fun `should parse added event when payload has 8 bytes`() {
    val bytes = buildNotificationSourceBytes(NotificationEventKind.ADDED, uid = 0x42, category = AncsCategory.SOCIAL)

    val event = NotificationSourceParser.parse(bytes)!!

    assertEquals(NotificationEventKind.ADDED, event.kind)
    assertEquals(0x42L, event.notificationUid)
    assertEquals(AncsCategory.SOCIAL, event.category)
    assertFalse(event.isSilent)
    assertFalse(event.isPreExisting)
  }

  @Test
  fun `should parse removed event when event id is 2`() {
    val bytes = buildNotificationSourceBytes(NotificationEventKind.REMOVED, uid = 7)

    assertEquals(NotificationEventKind.REMOVED, NotificationSourceParser.parse(bytes)!!.kind)
  }

  @Test
  fun `should read every flag when all flags are set`() {
    val allFlags = FLAG_SILENT or FLAG_IMPORTANT or FLAG_PRE_EXISTING or FLAG_POSITIVE_ACTION or FLAG_NEGATIVE_ACTION
    val bytes = buildNotificationSourceBytes(NotificationEventKind.MODIFIED, uid = 1, flags = allFlags)

    val event = NotificationSourceParser.parse(bytes)!!

    assertEquals(NotificationEventKind.MODIFIED, event.kind)
    assertTrue(event.isSilent)
    assertTrue(event.isImportant)
    assertTrue(event.isPreExisting)
    assertTrue(event.hasPositiveAction)
    assertTrue(event.hasNegativeAction)
  }

  @Test
  fun `should parse incoming call category when category id is 1`() {
    val bytes = buildNotificationSourceBytes(NotificationEventKind.ADDED, uid = 1, category = AncsCategory.INCOMING_CALL)

    assertEquals(AncsCategory.INCOMING_CALL, NotificationSourceParser.parse(bytes)!!.category)
  }

  @Test
  fun `should parse uid as unsigned when uid is max uint32`() {
    val bytes = buildNotificationSourceBytes(NotificationEventKind.ADDED, uid = 0xFFFFFFFFL)

    assertEquals(0xFFFFFFFFL, NotificationSourceParser.parse(bytes)!!.notificationUid)
  }

  @Test
  fun `should return null when payload is shorter than 8 bytes`() {
    assertNull(NotificationSourceParser.parse(byteArrayOf(0, 0, 0)))
  }

  @Test
  fun `should return null when payload is empty`() {
    assertNull(NotificationSourceParser.parse(byteArrayOf()))
  }

  @Test
  fun `should return null when event id is unknown`() {
    val bytes = buildNotificationSourceBytes(NotificationEventKind.ADDED, uid = 1).also { it[0] = 9 }

    assertNull(NotificationSourceParser.parse(bytes))
  }

  @Test
  fun `should map every known category when category ids are 0 to 11`() {
    AncsCategory.entries.forEach { category ->
      val bytes = buildNotificationSourceBytes(NotificationEventKind.ADDED, uid = 1, category = category)
      assertEquals(category, NotificationSourceParser.parse(bytes)!!.category)
    }
  }

  @Test
  fun `should fall back to other category when category id is unknown`() {
    val bytes = buildNotificationSourceBytes(NotificationEventKind.ADDED, uid = 1).also { it[2] = 99 }

    assertEquals(AncsCategory.OTHER, NotificationSourceParser.parse(bytes)!!.category)
  }
}
