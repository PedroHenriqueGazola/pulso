package app.pulso.watch.ancs

enum class AncsCategory(val id: Int) {
  OTHER(0),
  INCOMING_CALL(1),
  MISSED_CALL(2),
  VOICEMAIL(3),
  SOCIAL(4),
  SCHEDULE(5),
  EMAIL(6),
  NEWS(7),
  HEALTH_AND_FITNESS(8),
  BUSINESS_AND_FINANCE(9),
  LOCATION(10),
  ENTERTAINMENT(11);

  companion object {
    fun fromId(id: Int): AncsCategory = entries.firstOrNull { it.id == id } ?: OTHER
  }
}
