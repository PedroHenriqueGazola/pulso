package app.pulso.watch.ams

fun buildEntityUpdateBytes(entityId: Int, attributeId: Int, value: String): ByteArray =
  byteArrayOf(entityId.toByte(), attributeId.toByte(), 0) + value.toByteArray(Charsets.UTF_8)
