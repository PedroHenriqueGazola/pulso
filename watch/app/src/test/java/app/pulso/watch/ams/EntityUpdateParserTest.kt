package app.pulso.watch.ams

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EntityUpdateParserTest {
  @Test
  fun `should parse playing state when playback info starts with 1`() {
    assertEquals(EntityUpdate.PlaybackInfo(isPlaying = true), EntityUpdateParser.parse(buildEntityUpdateBytes(0, 1, "1,1.0,12.5")))
  }

  @Test
  fun `should parse paused state when playback info starts with 0`() {
    assertEquals(EntityUpdate.PlaybackInfo(isPlaying = false), EntityUpdateParser.parse(buildEntityUpdateBytes(0, 1, "0,0.0,12.5")))
  }

  @Test
  fun `should parse player name when entity is player and attribute is name`() {
    assertEquals(EntityUpdate.PlayerName("Spotify"), EntityUpdateParser.parse(buildEntityUpdateBytes(0, 0, "Spotify")))
  }

  @Test
  fun `should parse volume when entity is player and attribute is volume`() {
    assertEquals(EntityUpdate.Volume(0.5f), EntityUpdateParser.parse(buildEntityUpdateBytes(0, 2, "0.5")))
  }

  @Test
  fun `should parse track title when entity is track and attribute is title`() {
    assertEquals(EntityUpdate.TrackTitle("Águas de Março"), EntityUpdateParser.parse(buildEntityUpdateBytes(2, 2, "Águas de Março")))
  }

  @Test
  fun `should parse track artist when entity is track and attribute is artist`() {
    assertEquals(EntityUpdate.TrackArtist("Elis Regina"), EntityUpdateParser.parse(buildEntityUpdateBytes(2, 0, "Elis Regina")))
  }

  @Test
  fun `should return null when payload is shorter than 3 bytes`() {
    assertNull(EntityUpdateParser.parse(byteArrayOf(0, 1)))
  }

  @Test
  fun `should return null when entity is queue`() {
    assertNull(EntityUpdateParser.parse(buildEntityUpdateBytes(1, 0, "3")))
  }
}
