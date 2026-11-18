package app.pulso.watch.notifications

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class VectorDrawablePathDataTest {
  private val drawableDirectory = File("src/main/res/drawable")
  private val pathDataPattern = Regex("""android:pathData="([^"]+)"""")
  private val tokenPattern = Regex("""[a-zA-Z]|[-+]?(?:\d+\.?\d*|\.\d+)(?:[eE][-+]?\d+)?""")
  private val argumentCounts = mapOf(
    'm' to 2, 'l' to 2, 't' to 2, 'h' to 1, 'v' to 1, 'c' to 6, 's' to 4, 'q' to 4, 'a' to 7, 'z' to 0,
  )

  @Test
  fun `should have argument counts android accepts when drawable has path data`() {
    val drawables = drawableDirectory.listFiles { file -> file.extension == "xml" }.orEmpty()
    assertTrue("Nenhum drawable encontrado em ${drawableDirectory.absolutePath}", drawables.isNotEmpty())

    val invalidPaths = drawables.flatMap { drawable ->
      pathDataPattern.findAll(drawable.readText())
        .map { it.groupValues[1] }
        .flatMap { findInvalidCommands(it) }
        .map { "${drawable.name}: $it" }
        .toList()
    }

    assertEquals(emptyList<String>(), invalidPaths)
  }

  private fun findInvalidCommands(pathData: String): List<String> {
    val tokens = tokenPattern.findAll(pathData).map { it.value }.toList()
    val commandIndexes = tokens.indices.filter { tokens[it].first().isLetter() }
    return commandIndexes.mapIndexedNotNull { position, commandIndex ->
      val nextCommandIndex = commandIndexes.getOrElse(position + 1) { tokens.size }
      val command = tokens[commandIndex].first()
      val floatCount = nextCommandIndex - commandIndex - 1
      val argumentCount = argumentCounts.getValue(command.lowercaseChar())
      val isValid = if (argumentCount == 0) floatCount == 0 else floatCount % argumentCount == 0 && floatCount > 0
      if (isValid) null else "comando '$command' com $floatCount números (esperado múltiplo de $argumentCount)"
    }
  }
}
