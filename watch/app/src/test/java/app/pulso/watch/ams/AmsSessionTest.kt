package app.pulso.watch.ams

import app.pulso.watch.bluetooth.FakeGattClient
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AmsSessionTest {
  private fun List<Pair<java.util.UUID, ByteArray>>.unsignedValues() =
    map { (characteristic, value) -> characteristic to value.map { it.toInt() and 0xFF } }

  @Test
  fun `should register player and track attributes when session starts`() = runTest {
    val gattClient = FakeGattClient(services = setOf(AmsUuids.SERVICE))

    AmsSession(gattClient, backgroundScope).start()

    assertEquals(listOf(AmsUuids.ENTITY_UPDATE), gattClient.enabledNotifications)
    val expected = listOf(
      AmsUuids.ENTITY_UPDATE to listOf(0, 0, 1, 2),
      AmsUuids.ENTITY_UPDATE to listOf(2, 0, 2),
    )
    assertEquals(expected, gattClient.writes.unsignedValues())
  }

  @Test
  fun `should write toggle command when sending play pause`() = runTest {
    val gattClient = FakeGattClient(services = setOf(AmsUuids.SERVICE))
    val session = AmsSession(gattClient, backgroundScope)

    session.send(MediaCommand.TOGGLE_PLAY_PAUSE)

    assertEquals(AmsUuids.REMOTE_COMMAND to listOf(2), gattClient.writes.unsignedValues().last())
  }

  @Test
  fun `should update state when entity update arrives`() = runTest {
    val gattClient = FakeGattClient(services = setOf(AmsUuids.SERVICE))
    val session = AmsSession(gattClient, backgroundScope)
    session.start()

    gattClient.emit(AmsUuids.ENTITY_UPDATE, buildEntityUpdateBytes(2, 2, "Garota de Ipanema"))
    gattClient.emit(AmsUuids.ENTITY_UPDATE, buildEntityUpdateBytes(0, 1, "1,1.0,3.0"))
    runCurrent()

    assertEquals("Garota de Ipanema", session.state.value.title)
    assertTrue(session.state.value.isPlaying)
  }

  @Test
  fun `should not fail when iphone has no ams service`() = runTest {
    val gattClient = FakeGattClient(services = emptySet())

    AmsSession(gattClient, backgroundScope).start()

    assertTrue(gattClient.enabledNotifications.isEmpty())
    assertTrue(gattClient.writes.isEmpty())
  }
}
