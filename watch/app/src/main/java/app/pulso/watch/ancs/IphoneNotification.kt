package app.pulso.watch.ancs

data class IphoneNotification(
  val uid: Long,
  val appIdentifier: String,
  val title: String,
  val subtitle: String,
  val message: String,
  val category: AncsCategory,
  val isSilent: Boolean,
  val hasPositiveAction: Boolean,
  val hasNegativeAction: Boolean,
  val positiveActionLabel: String,
  val negativeActionLabel: String,
)
