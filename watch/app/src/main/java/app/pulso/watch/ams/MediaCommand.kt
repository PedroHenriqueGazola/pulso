package app.pulso.watch.ams

enum class MediaCommand(val id: Int) {
  PLAY(0),
  PAUSE(1),
  TOGGLE_PLAY_PAUSE(2),
  NEXT_TRACK(3),
  PREVIOUS_TRACK(4),
  VOLUME_UP(5),
  VOLUME_DOWN(6),
}
