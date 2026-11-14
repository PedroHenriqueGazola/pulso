package app.pulso.watch.ancs

internal fun ByteArray.readUnsignedByte(offset: Int): Int = this[offset].toInt() and 0xFF

internal fun ByteArray.readUInt16LittleEndian(offset: Int): Int =
  readUnsignedByte(offset) or (readUnsignedByte(offset + 1) shl 8)

internal fun ByteArray.readUInt32LittleEndian(offset: Int): Long =
  (0 until 4).fold(0L) { value, index -> value or (readUnsignedByte(offset + index).toLong() shl (8 * index)) }

internal fun uInt32LittleEndianBytes(value: Long): List<Int> =
  (0 until 4).map { index -> ((value shr (8 * index)) and 0xFF).toInt() }
