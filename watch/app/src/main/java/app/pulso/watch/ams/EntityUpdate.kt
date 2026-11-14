package app.pulso.watch.ams

sealed interface EntityUpdate {
  data class PlayerName(val name: String) : EntityUpdate

  data class PlaybackInfo(val isPlaying: Boolean) : EntityUpdate

  data class Volume(val level: Float) : EntityUpdate

  data class TrackArtist(val artist: String) : EntityUpdate

  data class TrackAlbum(val album: String) : EntityUpdate

  data class TrackTitle(val title: String) : EntityUpdate

  data class TrackDuration(val seconds: Float) : EntityUpdate
}
