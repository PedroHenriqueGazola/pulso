package app.pulso.watch.calls

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.VibrationEffect
import android.os.VibratorManager
import android.telecom.DisconnectCause
import android.telecom.PhoneAccount
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.util.Log
import androidx.core.app.NotificationCompat
import app.pulso.watch.notifications.NotificationChannels

class CallPresenter(private val context: Context) {
  private val notificationManager = context.getSystemService(NotificationManager::class.java)
  private val vibrator = context.getSystemService(VibratorManager::class.java).defaultVibrator
  private val telecomManager = context.getSystemService(TelecomManager::class.java)
  private val phoneAccountHandle = PhoneAccountHandle(ComponentName(context, PulsoConnectionService::class.java), "pulso")

  fun render(state: CallState) {
    when (state) {
      is CallState.Ringing -> showRinging(state)
      is CallState.ActiveOnIphone -> stopAlerting()
      CallState.Idle -> endCall()
    }
  }

  private fun showRinging(state: CallState.Ringing) {
    registerIncomingCallWithTelecom(state.uid)
    notificationManager.notify(CALL_NOTIFICATION_ID, buildFullScreenNotification(state))
    context.startActivity(buildCallScreenIntent())
    vibrator.vibrate(VibrationEffect.createWaveform(RINGING_PATTERN, NO_REPEAT))
  }

  private fun stopAlerting() {
    vibrator.cancel()
    notificationManager.cancel(CALL_NOTIFICATION_ID)
  }

  private fun endCall() {
    stopAlerting()
    PulsoConnectionService.currentConnection?.end(DisconnectCause.REMOTE)
    PulsoConnectionService.currentConnection = null
  }

  private fun registerIncomingCallWithTelecom(uid: Long) {
    runCatching {
      telecomManager.registerPhoneAccount(
        PhoneAccount.builder(phoneAccountHandle, "Pulso").setCapabilities(PhoneAccount.CAPABILITY_SELF_MANAGED).build(),
      )
      val extras = Bundle().apply { putLong(PulsoConnectionService.EXTRA_CALL_UID, uid) }
      telecomManager.addNewIncomingCall(phoneAccountHandle, extras)
    }.onFailure { error -> Log.w("Pulso", "Telecom indisponível; usando só a tela própria", error) }
  }

  private fun buildFullScreenNotification(state: CallState.Ringing) =
    NotificationCompat.Builder(context, NotificationChannels.CALLS)
      .setSmallIcon(android.R.drawable.sym_call_incoming)
      .setContentTitle(state.callerName)
      .setContentText("Ligação pelo ${state.appLabel}")
      .setCategory(NotificationCompat.CATEGORY_CALL)
      .setPriority(NotificationCompat.PRIORITY_MAX)
      .setOngoing(true)
      .setFullScreenIntent(buildCallScreenPendingIntent(), true)
      .build()

  private fun buildCallScreenIntent() = Intent(context, IncomingCallActivity::class.java)
    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)

  private fun buildCallScreenPendingIntent() = PendingIntent.getActivity(
    context,
    CALL_NOTIFICATION_ID,
    buildCallScreenIntent(),
    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
  )

  private companion object {
    const val CALL_NOTIFICATION_ID = 2
    const val NO_REPEAT = -1
    const val RING_CYCLES = 40
    val RINGING_PATTERN = longArrayOf(0) + List(RING_CYCLES) { listOf(800L, 600L) }.flatten()
  }
}
