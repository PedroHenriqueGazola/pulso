package app.pulso.watch.ancs

import org.junit.Assert.assertEquals
import org.junit.Test

class ControlPointCommandsTest {
  private fun ByteArray.unsigned(): List<Int> = map { it.toInt() and 0xFF }

  @Test
  fun `should request standard attributes when action labels are not included`() {
    val command = ControlPointCommands.buildGetNotificationAttributes(0x42, shouldIncludeActionLabels = false)

    val expected = listOf(
      0, 0x42, 0, 0, 0,
      0,
      1, 0xFF, 0,
      2, 0xFF, 0,
      3, 0xFF, 0,
      5,
    )
    assertEquals(expected, command.unsigned())
  }

  @Test
  fun `should request action labels when action labels are included`() {
    val command = ControlPointCommands.buildGetNotificationAttributes(1, shouldIncludeActionLabels = true)

    val actionLabelBytes = command.unsigned().takeLast(6)
    assertEquals(listOf(6, 0xFF, 0, 7, 0xFF, 0), actionLabelBytes)
  }

  @Test
  fun `should list requested attribute ids in request order when action labels are included`() {
    assertEquals(listOf(0, 1, 2, 3, 5, 6, 7), ControlPointCommands.requestedAttributeIds(shouldIncludeActionLabels = true))
    assertEquals(listOf(0, 1, 2, 3, 5), ControlPointCommands.requestedAttributeIds(shouldIncludeActionLabels = false))
  }

  @Test
  fun `should encode uid little endian when building attributes request`() {
    val command = ControlPointCommands.buildGetNotificationAttributes(0x01020304, shouldIncludeActionLabels = false)

    assertEquals(listOf(0x04, 0x03, 0x02, 0x01), command.unsigned().subList(1, 5))
  }

  @Test
  fun `should build positive action command when performing positive action`() {
    val command = ControlPointCommands.buildPerformNotificationAction(0x42, NotificationAction.POSITIVE)

    assertEquals(listOf(2, 0x42, 0, 0, 0, 0), command.unsigned())
  }

  @Test
  fun `should build negative action command when performing dismiss`() {
    val command = ControlPointCommands.buildPerformNotificationAction(0x01020304, NotificationAction.NEGATIVE)

    assertEquals(listOf(2, 0x04, 0x03, 0x02, 0x01, 1), command.unsigned())
  }
}
