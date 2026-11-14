package app.pulso.watch.ancs

object NotificationSourceParser {
  private const val PAYLOAD_LENGTH = 8
  private const val FLAG_SILENT = 0x01
  private const val FLAG_IMPORTANT = 0x02
  private const val FLAG_PRE_EXISTING = 0x04
  private const val FLAG_POSITIVE_ACTION = 0x08
  private const val FLAG_NEGATIVE_ACTION = 0x10

  fun parse(bytes: ByteArray): NotificationSourceEvent? {
    if (bytes.size < PAYLOAD_LENGTH) return null
    val kind = NotificationEventKind.entries.getOrNull(bytes.readUnsignedByte(0)) ?: return null
    val flags = bytes.readUnsignedByte(1)

    return NotificationSourceEvent(
      kind = kind,
      notificationUid = bytes.readUInt32LittleEndian(4),
      category = AncsCategory.fromId(bytes.readUnsignedByte(2)),
      isSilent = flags.hasFlag(FLAG_SILENT),
      isImportant = flags.hasFlag(FLAG_IMPORTANT),
      isPreExisting = flags.hasFlag(FLAG_PRE_EXISTING),
      hasPositiveAction = flags.hasFlag(FLAG_POSITIVE_ACTION),
      hasNegativeAction = flags.hasFlag(FLAG_NEGATIVE_ACTION),
    )
  }

  private fun Int.hasFlag(flag: Int): Boolean = this and flag != 0
}
