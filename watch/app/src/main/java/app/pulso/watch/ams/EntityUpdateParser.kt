package app.pulso.watch.ams

object EntityUpdateParser {
  private const val HEADER_LENGTH = 3
  private const val ENTITY_PLAYER = 0
  private const val ENTITY_TRACK = 2
  private const val PLAYBACK_STATE_PLAYING = "1"

  private val BUILDERS: Map<Pair<Int, Int>, (String) -> EntityUpdate?> = mapOf(
    (ENTITY_PLAYER to 0) to { value -> EntityUpdate.PlayerName(value) },
    (ENTITY_PLAYER to 1) to { value -> EntityUpdate.PlaybackInfo(isPlaying = value.substringBefore(',') == PLAYBACK_STATE_PLAYING) },
    (ENTITY_PLAYER to 2) to { value -> value.toFloatOrNull()?.let(EntityUpdate::Volume) },
    (ENTITY_TRACK to 0) to { value -> EntityUpdate.TrackArtist(value) },
    (ENTITY_TRACK to 1) to { value -> EntityUpdate.TrackAlbum(value) },
    (ENTITY_TRACK to 2) to { value -> EntityUpdate.TrackTitle(value) },
    (ENTITY_TRACK to 3) to { value -> value.toFloatOrNull()?.let(EntityUpdate::TrackDuration) },
  )

  fun parse(bytes: ByteArray): EntityUpdate? {
    if (bytes.size < HEADER_LENGTH) return null
    val entityId = bytes[0].toInt() and 0xFF
    val attributeId = bytes[1].toInt() and 0xFF
    val build = BUILDERS[entityId to attributeId] ?: return null
    val value = String(bytes, HEADER_LENGTH, bytes.size - HEADER_LENGTH, Charsets.UTF_8)
    return build(value)
  }
}
