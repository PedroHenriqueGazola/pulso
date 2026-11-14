package app.pulso.watch.bluetooth

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class GattOperationQueue {
  private val mutex = Mutex()

  suspend fun <T> run(operation: suspend () -> T): T = mutex.withLock { operation() }
}
