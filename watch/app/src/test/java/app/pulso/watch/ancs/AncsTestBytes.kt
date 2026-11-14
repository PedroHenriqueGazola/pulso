package app.pulso.watch.ancs

import java.io.ByteArrayOutputStream

object AncsTestBytes {
  const val FLAG_SILENT = 0x01
  const val FLAG_IMPORTANT = 0x02
  const val FLAG_PRE_EXISTING = 0x04
  const val FLAG_POSITIVE_ACTION = 0x08
  const val FLAG_NEGATIVE_ACTION = 0x10

  fun buildNotificationSourceBytes(
    kind: NotificationEventKind,
    uid: Long,
    category: AncsCategory = AncsCategory.SOCIAL,
    flags: Int = 0,
  ): ByteArray {
    val eventId = kind.ordinal
    return byteArrayOf(
      eventId.toByte(),
      flags.toByte(),
      category.id.toByte(),
      1,
      (uid and 0xFF).toByte(),
      ((uid shr 8) and 0xFF).toByte(),
      ((uid shr 16) and 0xFF).toByte(),
      ((uid shr 24) and 0xFF).toByte(),
    )
  }

  fun buildAttributesResponse(uid: Long, attributes: List<Pair<Int, String>>): ByteArray {
    val buffer = ByteArrayOutputStream()
    buffer.write(0)
    writeUInt32LittleEndian(buffer, uid)
    attributes.forEach { (attributeId, value) ->
      val bytes = value.toByteArray(Charsets.UTF_8)
      buffer.write(attributeId)
      buffer.write(bytes.size and 0xFF)
      buffer.write((bytes.size shr 8) and 0xFF)
      buffer.write(bytes)
    }
    return buffer.toByteArray()
  }

  fun buildStandardAttributesResponse(
    uid: Long,
    appIdentifier: String,
    title: String,
    message: String,
    subtitle: String = "",
  ): ByteArray = buildAttributesResponse(
    uid,
    listOf(0 to appIdentifier, 1 to title, 2 to subtitle, 3 to message, 5 to "20261114T120000"),
  )

  private fun writeUInt32LittleEndian(buffer: ByteArrayOutputStream, value: Long) {
    (0 until 4).forEach { index -> buffer.write(((value shr (8 * index)) and 0xFF).toInt()) }
  }
}
