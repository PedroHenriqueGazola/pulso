package app.pulso.watch.ancs

import app.pulso.watch.ancs.AncsTestBytes.FLAG_NEGATIVE_ACTION
import app.pulso.watch.ancs.AncsTestBytes.FLAG_SILENT
import app.pulso.watch.ancs.AncsTestBytes.buildAttributesResponse
import app.pulso.watch.ancs.AncsTestBytes.buildNotificationSourceBytes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationAttributesAssemblerTest {
  private val assembler = NotificationAttributesAssembler()

  private fun eventFor(uid: Long, category: AncsCategory = AncsCategory.SOCIAL, flags: Int = 0) =
    NotificationSourceParser.parse(buildNotificationSourceBytes(NotificationEventKind.ADDED, uid, category, flags))!!

  @Test
  fun `should assemble notification when response arrives in a single fragment`() {
    assembler.expect(eventFor(0x42), listOf(0, 1, 3))

    val notification = assembler.append(
      buildAttributesResponse(0x42, listOf(0 to "com.apple.MobileSMS", 1 to "John", 3 to "Hello world")),
    )!!

    assertEquals(0x42L, notification.uid)
    assertEquals("com.apple.MobileSMS", notification.appIdentifier)
    assertEquals("John", notification.title)
    assertEquals("Hello world", notification.message)
    assertEquals(AncsCategory.SOCIAL, notification.category)
  }

  @Test
  fun `should assemble notification when response spans multiple fragments`() {
    assembler.expect(eventFor(1, AncsCategory.EMAIL), listOf(0, 1))
    val response = buildAttributesResponse(1, listOf(0 to "com.google.Gmail", 1 to "Alice"))

    assertNull(assembler.append(response.copyOfRange(0, 8)))
    assertNull(assembler.append(response.copyOfRange(8, 20)))
    val notification = assembler.append(response.copyOfRange(20, response.size))!!

    assertEquals("com.google.Gmail", notification.appIdentifier)
    assertEquals("Alice", notification.title)
    assertEquals(AncsCategory.EMAIL, notification.category)
  }

  @Test
  fun `should return empty strings when attribute values are empty`() {
    assembler.expect(eventFor(5), listOf(0, 1, 2))

    val notification = assembler.append(buildAttributesResponse(5, listOf(0 to "com.app.test", 1 to "", 2 to "")))!!

    assertEquals("", notification.title)
    assertEquals("", notification.subtitle)
  }

  @Test
  fun `should keep partial state when fragment ends inside length field`() {
    assembler.expect(eventFor(1), listOf(0, 1))
    val response = buildAttributesResponse(1, listOf(0 to "app", 1 to "Bob"))

    assertNull(assembler.append(response.copyOfRange(0, 13)))
    val notification = assembler.append(response.copyOfRange(13, response.size))!!

    assertEquals("Bob", notification.title)
  }

  @Test
  fun `should decode multibyte text when utf8 character is split across fragments`() {
    val message = "Olá 👋 tudo bem?"
    assembler.expect(eventFor(3), listOf(0, 3))
    val response = buildAttributesResponse(3, listOf(0 to "net.whatsapp.WhatsApp", 3 to message))
    val emojiStart = response.size - "👋 tudo bem?".toByteArray().size
    val splitInsideEmoji = emojiStart + 2

    assertNull(assembler.append(response.copyOfRange(0, splitInsideEmoji)))
    val notification = assembler.append(response.copyOfRange(splitInsideEmoji, response.size))!!

    assertEquals(message, notification.message)
  }

  @Test
  fun `should start fresh when reset after partial data`() {
    assembler.expect(eventFor(1), listOf(0))
    assembler.append(byteArrayOf(0x00, 0x01, 0x00, 0x00))

    assembler.reset()
    assembler.expect(eventFor(2), listOf(0))
    val notification = assembler.append(buildAttributesResponse(2, listOf(0 to "test")))!!

    assertEquals(2L, notification.uid)
  }

  @Test
  fun `should carry event flags when notification is assembled`() {
    assembler.expect(eventFor(8, flags = FLAG_SILENT or FLAG_NEGATIVE_ACTION), listOf(0, 7))

    val notification = assembler.append(buildAttributesResponse(8, listOf(0 to "com.apple.mobilemail", 7 to "Apagar")))!!

    assertTrue(notification.isSilent)
    assertTrue(notification.hasNegativeAction)
    assertEquals("Apagar", notification.negativeActionLabel)
  }

  @Test
  fun `should ignore response when uid does not match expected notification`() {
    assembler.expect(eventFor(1), listOf(0))

    assertNull(assembler.append(buildAttributesResponse(99, listOf(0 to "other"))))
  }
}
