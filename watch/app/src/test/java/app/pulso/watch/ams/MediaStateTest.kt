package app.pulso.watch.ams

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MediaStateTest {
  @Test
  fun `should keep other fields when applying title update`() {
    val state = MediaState(playerName = "Spotify", artist = "Elis Regina", isPlaying = true)

    val updated = state.apply(EntityUpdate.TrackTitle("Como Nossos Pais"))

    assertEquals(MediaState(playerName = "Spotify", title = "Como Nossos Pais", artist = "Elis Regina", isPlaying = true), updated)
  }

  @Test
  fun `should report no active player when player name is empty`() {
    assertFalse(MediaState().hasActivePlayer)
  }

  @Test
  fun `should report active player when player name is set`() {
    assertTrue(MediaState().apply(EntityUpdate.PlayerName("Music")).hasActivePlayer)
  }
}
