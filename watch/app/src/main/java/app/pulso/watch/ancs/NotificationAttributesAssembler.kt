package app.pulso.watch.ancs

import app.pulso.watch.ancs.NotificationAttributeId.APP_IDENTIFIER
import app.pulso.watch.ancs.NotificationAttributeId.MESSAGE
import app.pulso.watch.ancs.NotificationAttributeId.NEGATIVE_ACTION_LABEL
import app.pulso.watch.ancs.NotificationAttributeId.POSITIVE_ACTION_LABEL
import app.pulso.watch.ancs.NotificationAttributeId.SUBTITLE
import app.pulso.watch.ancs.NotificationAttributeId.TITLE
import java.io.ByteArrayOutputStream

class NotificationAttributesAssembler {
  private var expectedEvent: NotificationSourceEvent? = null
  private var expectedAttributeIds: List<Int> = emptyList()
  private val receivedBytes = ByteArrayOutputStream()

  fun expect(event: NotificationSourceEvent, requestedAttributeIds: List<Int>) {
    reset()
    expectedEvent = event
    expectedAttributeIds = requestedAttributeIds
  }

  fun append(fragment: ByteArray): IphoneNotification? {
    val event = expectedEvent ?: return null
    receivedBytes.write(fragment)
    val response = receivedBytes.toByteArray()
    if (response.size < HEADER_LENGTH) return null
    if (response.readUInt32LittleEndian(1) != event.notificationUid) {
      receivedBytes.reset()
      return null
    }
    val attributes = parseAttributes(response) ?: return null
    reset()
    return buildNotification(event, attributes)
  }

  fun reset() {
    expectedEvent = null
    expectedAttributeIds = emptyList()
    receivedBytes.reset()
  }

  private fun parseAttributes(response: ByteArray): Map<Int, String>? {
    val attributes = mutableMapOf<Int, String>()
    var offset = HEADER_LENGTH
    repeat(expectedAttributeIds.size) {
      if (offset + ATTRIBUTE_HEADER_LENGTH > response.size) return null
      val attributeId = response.readUnsignedByte(offset)
      val valueLength = response.readUInt16LittleEndian(offset + 1)
      val valueStart = offset + ATTRIBUTE_HEADER_LENGTH
      if (valueStart + valueLength > response.size) return null
      attributes[attributeId] = String(response, valueStart, valueLength, Charsets.UTF_8)
      offset = valueStart + valueLength
    }
    return attributes
  }

  private fun buildNotification(event: NotificationSourceEvent, attributes: Map<Int, String>) = IphoneNotification(
    uid = event.notificationUid,
    appIdentifier = attributes[APP_IDENTIFIER].orEmpty(),
    title = attributes[TITLE].orEmpty(),
    subtitle = attributes[SUBTITLE].orEmpty(),
    message = attributes[MESSAGE].orEmpty(),
    category = event.category,
    isSilent = event.isSilent,
    hasPositiveAction = event.hasPositiveAction,
    hasNegativeAction = event.hasNegativeAction,
    positiveActionLabel = attributes[POSITIVE_ACTION_LABEL].orEmpty(),
    negativeActionLabel = attributes[NEGATIVE_ACTION_LABEL].orEmpty(),
  )

  private companion object {
    const val HEADER_LENGTH = 5
    const val ATTRIBUTE_HEADER_LENGTH = 3
  }
}
