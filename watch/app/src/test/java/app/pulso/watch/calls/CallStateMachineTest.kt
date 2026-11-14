package app.pulso.watch.calls

import app.pulso.watch.ancs.AncsCategory
import app.pulso.watch.ancs.IphoneNotification
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class CallStateMachineTest {
  private val machine = CallStateMachine()

  private fun call(uid: Long = 10, hasPositiveAction: Boolean = true, hasNegativeAction: Boolean = true) = IphoneNotification(
    uid = uid,
    appIdentifier = "com.apple.mobilephone",
    title = "Ana",
    subtitle = "",
    message = "Chamada recebida",
    category = AncsCategory.INCOMING_CALL,
    isSilent = false,
    hasPositiveAction = hasPositiveAction,
    hasNegativeAction = hasNegativeAction,
    positiveActionLabel = "Atender",
    negativeActionLabel = "Recusar",
  )

  @Test
  fun `should ring when call is posted`() {
    machine.onCallPosted(call())

    assertEquals(
      CallState.Ringing(uid = 10, callerName = "Ana", appLabel = "Telefone", canAnswer = true, canDecline = true),
      machine.state.value,
    )
  }

  @Test
  fun `should move to active on iphone when ringing call is modified`() {
    machine.onCallPosted(call())

    machine.onCallChanged(call())

    assertEquals(CallState.ActiveOnIphone(uid = 10, callerName = "Ana"), machine.state.value)
  }

  @Test
  fun `should return to idle when call is removed`() {
    machine.onCallPosted(call())

    machine.onRemoved(10)

    assertEquals(CallState.Idle, machine.state.value)
  }

  @Test
  fun `should ignore removal when uid is not the current call`() {
    machine.onCallPosted(call(uid = 10))

    machine.onRemoved(99)

    assertEquals(10L, (machine.state.value as CallState.Ringing).uid)
  }

  @Test
  fun `should hide answer when call has no positive action`() {
    machine.onCallPosted(call(hasPositiveAction = false))

    assertFalse((machine.state.value as CallState.Ringing).canAnswer)
  }

  @Test
  fun `should return to idle when call is declined on watch`() {
    machine.onCallPosted(call())

    machine.onDeclinedOnWatch()

    assertEquals(CallState.Idle, machine.state.value)
  }

  @Test
  fun `should ring when modified event arrives for a call never seen as posted`() {
    machine.onCallChanged(call(uid = 11))

    assertEquals(11L, (machine.state.value as CallState.Ringing).uid)
  }

  @Test
  fun `should return to idle when reset after connection loss`() {
    machine.onCallPosted(call())

    machine.reset()

    assertEquals(CallState.Idle, machine.state.value)
  }
}
