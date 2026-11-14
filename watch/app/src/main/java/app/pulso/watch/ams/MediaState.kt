package app.pulso.watch.ams

data class MediaState(
  val playerName: String = "",
  val title: String = "",
  val artist: String = "",
  val isPlaying: Boolean = false,
  val volume: Float = 0f,
) {
  val hasActivePlayer: Boolean get() = playerName.isNotEmpty()

  fun apply(update: EntityUpdate): MediaState = when (update) {
    is EntityUpdate.PlayerName -> copy(playerName = update.name)
    is EntityUpdate.PlaybackInfo -> copy(isPlaying = update.isPlaying)
    is EntityUpdate.Volume -> copy(volume = update.level)
    is EntityUpdate.TrackArtist -> copy(artist = update.artist)
    is EntityUpdate.TrackTitle -> copy(title = update.title)
    is EntityUpdate.TrackAlbum, is EntityUpdate.TrackDuration -> this
  }
}
