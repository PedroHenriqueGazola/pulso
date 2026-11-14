package app.pulso.watch.ancs

object NotificationAttributeId {
  const val APP_IDENTIFIER = 0
  const val TITLE = 1
  const val SUBTITLE = 2
  const val MESSAGE = 3
  const val DATE = 5
  const val POSITIVE_ACTION_LABEL = 6
  const val NEGATIVE_ACTION_LABEL = 7

  val ATTRIBUTES_WITH_MAX_LENGTH = setOf(TITLE, SUBTITLE, MESSAGE, POSITIVE_ACTION_LABEL, NEGATIVE_ACTION_LABEL)
}
