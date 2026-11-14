package app.pulso.watch.ancs

import app.pulso.watch.ancs.NotificationAttributeId.APP_IDENTIFIER
import app.pulso.watch.ancs.NotificationAttributeId.ATTRIBUTES_WITH_MAX_LENGTH
import app.pulso.watch.ancs.NotificationAttributeId.DATE
import app.pulso.watch.ancs.NotificationAttributeId.MESSAGE
import app.pulso.watch.ancs.NotificationAttributeId.NEGATIVE_ACTION_LABEL
import app.pulso.watch.ancs.NotificationAttributeId.POSITIVE_ACTION_LABEL
import app.pulso.watch.ancs.NotificationAttributeId.SUBTITLE
import app.pulso.watch.ancs.NotificationAttributeId.TITLE

enum class NotificationAction(val id: Int) {
  POSITIVE(0),
  NEGATIVE(1),
}

object ControlPointCommands {
  private const val COMMAND_GET_NOTIFICATION_ATTRIBUTES = 0
  private const val COMMAND_PERFORM_NOTIFICATION_ACTION = 2
  private const val MAX_ATTRIBUTE_LENGTH = 255

  private val STANDARD_ATTRIBUTE_IDS = listOf(APP_IDENTIFIER, TITLE, SUBTITLE, MESSAGE, DATE)
  private val ACTION_LABEL_ATTRIBUTE_IDS = listOf(POSITIVE_ACTION_LABEL, NEGATIVE_ACTION_LABEL)

  fun requestedAttributeIds(shouldIncludeActionLabels: Boolean): List<Int> {
    if (!shouldIncludeActionLabels) return STANDARD_ATTRIBUTE_IDS
    return STANDARD_ATTRIBUTE_IDS + ACTION_LABEL_ATTRIBUTE_IDS
  }

  fun buildGetNotificationAttributes(notificationUid: Long, shouldIncludeActionLabels: Boolean): ByteArray {
    val attributeBytes = requestedAttributeIds(shouldIncludeActionLabels).flatMap(::encodeAttributeRequest)
    val commandBytes = listOf(COMMAND_GET_NOTIFICATION_ATTRIBUTES) + uInt32LittleEndianBytes(notificationUid)
    return (commandBytes + attributeBytes).toByteArray()
  }

  fun buildPerformNotificationAction(notificationUid: Long, action: NotificationAction): ByteArray {
    val commandBytes = listOf(COMMAND_PERFORM_NOTIFICATION_ACTION) + uInt32LittleEndianBytes(notificationUid)
    return (commandBytes + action.id).toByteArray()
  }

  private fun encodeAttributeRequest(attributeId: Int): List<Int> {
    if (attributeId !in ATTRIBUTES_WITH_MAX_LENGTH) return listOf(attributeId)
    return listOf(attributeId, MAX_ATTRIBUTE_LENGTH and 0xFF, MAX_ATTRIBUTE_LENGTH shr 8)
  }

  private fun List<Int>.toByteArray(): ByteArray = map { it.toByte() }.toByteArray()
}
