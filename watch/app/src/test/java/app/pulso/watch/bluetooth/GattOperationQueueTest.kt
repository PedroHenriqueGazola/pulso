package app.pulso.watch.bluetooth

import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GattOperationQueueTest {
  private val queue = GattOperationQueue()

  @Test
  fun `should run operations one at a time when called concurrently`() = runTest {
    val log = mutableListOf<String>()

    val first = launch { queue.run { log += "start-a"; delay(10); log += "end-a" } }
    val second = launch { queue.run { log += "start-b"; delay(10); log += "end-b" } }
    first.join()
    second.join()

    assertEquals(listOf("start-a", "end-a", "start-b", "end-b"), log)
  }

  @Test
  fun `should release queue when operation throws`() = runTest {
    val failing = async { runCatching { queue.run { throw GattOperationException("write failed") } } }
    val succeeding = async { queue.run { "ok" } }

    assertEquals("write failed", failing.await().exceptionOrNull()?.message)
    assertEquals("ok", succeeding.await())
  }
}
