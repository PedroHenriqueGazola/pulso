package app.pulso.watch.ancs

enum class NotificationEventKind {
  ADDED,
  MODIFIED,
  REMOVED,
}

data class NotificationSourceEvent(
  val kind: NotificationEventKind,
  val notificationUid: Long,
  val category: AncsCategory,
  val isSilent: Boolean,
  val isImportant: Boolean,
  val isPreExisting: Boolean,
  val hasPositiveAction: Boolean,
  val hasNegativeAction: Boolean,
)
