package app.pulso.watch.ancs

import app.pulso.watch.bluetooth.CharacteristicChange
import app.pulso.watch.bluetooth.GattClient
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

class AncsSession(
  private val gattClient: GattClient,
  private val scope: CoroutineScope,
  private val attributesTimeoutMillis: Long = 5_000,
) {
  private val mutableUpdates = MutableSharedFlow<AncsUpdate>(extraBufferCapacity = 64)
  private val queuedEvents = Channel<NotificationSourceEvent>(Channel.UNLIMITED)
  private val dataSourceFragments = Channel<ByteArray>(Channel.UNLIMITED)
  private val pendingUids = ConcurrentHashMap.newKeySet<Long>()
  private val removedPendingUids = ConcurrentHashMap.newKeySet<Long>()
  private val assembler = NotificationAttributesAssembler()

  val updates: SharedFlow<AncsUpdate> = mutableUpdates

  suspend fun start() {
    scope.launch(start = CoroutineStart.UNDISPATCHED) {
      gattClient.characteristicChanges.collect(::handleCharacteristicChange)
    }
    scope.launch { processQueuedEvents() }
    gattClient.enableNotifications(AncsUuids.SERVICE, AncsUuids.DATA_SOURCE)
    gattClient.enableNotifications(AncsUuids.SERVICE, AncsUuids.NOTIFICATION_SOURCE)
  }

  suspend fun performAction(notificationUid: Long, action: NotificationAction) {
    val command = ControlPointCommands.buildPerformNotificationAction(notificationUid, action)
    gattClient.write(AncsUuids.SERVICE, AncsUuids.CONTROL_POINT, command)
  }

  private suspend fun handleCharacteristicChange(change: CharacteristicChange) {
    when (change.characteristic) {
      AncsUuids.NOTIFICATION_SOURCE -> handleNotificationSource(change.value)
      AncsUuids.DATA_SOURCE -> dataSourceFragments.send(change.value)
    }
  }

  private suspend fun handleNotificationSource(bytes: ByteArray) {
    val event = NotificationSourceParser.parse(bytes) ?: return
    if (event.isPreExisting) return
    if (event.kind == NotificationEventKind.REMOVED) return handleRemoved(event.notificationUid)

    pendingUids += event.notificationUid
    queuedEvents.send(event)
  }

  private suspend fun handleRemoved(uid: Long) {
    if (uid in pendingUids) removedPendingUids += uid
    mutableUpdates.emit(AncsUpdate.NotificationRemoved(uid))
  }

  private suspend fun processQueuedEvents() {
    for (event in queuedEvents) {
      val notification = fetchNotificationUnlessRemoved(event)
      pendingUids -= event.notificationUid
      val wasRemoved = removedPendingUids.remove(event.notificationUid)
      if (notification == null || wasRemoved) continue
      mutableUpdates.emit(buildUpdate(event, notification))
    }
  }

  private suspend fun fetchNotificationUnlessRemoved(event: NotificationSourceEvent): IphoneNotification? {
    if (event.notificationUid in removedPendingUids) return null
    return fetchNotification(event)
  }

  private suspend fun fetchNotification(event: NotificationSourceEvent): IphoneNotification? {
    discardStaleFragments()
    val shouldIncludeActionLabels = event.hasPositiveAction || event.hasNegativeAction
    assembler.expect(event, ControlPointCommands.requestedAttributeIds(shouldIncludeActionLabels))
    val command = ControlPointCommands.buildGetNotificationAttributes(event.notificationUid, shouldIncludeActionLabels)
    gattClient.write(AncsUuids.SERVICE, AncsUuids.CONTROL_POINT, command)

    return withTimeoutOrNull(attributesTimeoutMillis) { awaitAssembledNotification() }
  }

  private suspend fun awaitAssembledNotification(): IphoneNotification {
    for (fragment in dataSourceFragments) {
      val notification = assembler.append(fragment)
      if (notification != null) return notification
    }
    error("Data Source channel closed")
  }

  private fun discardStaleFragments() {
    while (dataSourceFragments.tryReceive().isSuccess) Unit
  }

  private fun buildUpdate(event: NotificationSourceEvent, notification: IphoneNotification): AncsUpdate {
    if (event.kind == NotificationEventKind.MODIFIED) return AncsUpdate.NotificationChanged(notification)
    return AncsUpdate.NotificationPosted(notification)
  }
}
