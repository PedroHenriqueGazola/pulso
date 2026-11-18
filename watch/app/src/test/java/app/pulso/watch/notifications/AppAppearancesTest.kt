package app.pulso.watch.notifications

import app.pulso.watch.ancs.AncsCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AppAppearancesTest {
  @Test
  fun `should return whatsapp icon and brand color when app is WhatsApp`() {
    val appearance = AppAppearances.appearanceFor("net.whatsapp.WhatsApp", AncsCategory.SOCIAL)

    assertEquals(AppAppearance("WhatsApp", NotificationIcon.WHATSAPP, 0xFF25D366.toInt()), appearance)
  }

  @Test
  fun `should return Nubank when app identifier is the real iOS bundle id`() {
    val appearance = AppAppearances.appearanceFor("com.nu.iphone", AncsCategory.BUSINESS_AND_FINANCE)

    assertEquals("Nubank", appearance.label)
    assertEquals(NotificationIcon.NUBANK, appearance.icon)
  }

  @Test
  fun `should return iFood when app identifier is the real iOS bundle id`() {
    assertEquals(NotificationIcon.IFOOD, AppAppearances.appearanceFor("br.com.brainweb.ifood", AncsCategory.OTHER).icon)
  }

  @Test
  fun `should return Mercado Livre with shopping icon when app has no brand icon`() {
    val appearance = AppAppearances.appearanceFor("com.3mosquitos.MercadoLibre", AncsCategory.OTHER)

    assertEquals("Mercado Livre", appearance.label)
    assertEquals(NotificationIcon.SHOPPING, appearance.icon)
  }

  @Test
  fun `should return category icon without color when app is unknown`() {
    val appearance = AppAppearances.appearanceFor("com.example.Agenda", AncsCategory.SCHEDULE)

    assertEquals(NotificationIcon.SCHEDULE, appearance.icon)
    assertNull(appearance.color)
  }

  @Test
  fun `should derive label from identifier when app is unknown`() {
    assertEquals("Agenda", AppAppearances.appearanceFor("com.example.Agenda", AncsCategory.SCHEDULE).label)
  }

  @Test
  fun `should return iPhone label when identifier is empty`() {
    assertEquals("iPhone", AppAppearances.appearanceFor("", AncsCategory.OTHER).label)
  }

  @Test
  fun `should return a category icon for every ancs category`() {
    val icons = AncsCategory.entries.map { AppAppearances.appearanceFor("com.example.Unknown", it).icon }

    assertEquals(AncsCategory.entries.size, icons.size)
    assertEquals(NotificationIcon.CALL, icons[AncsCategory.INCOMING_CALL.ordinal])
    assertEquals(NotificationIcon.EMAIL, icons[AncsCategory.EMAIL.ordinal])
    assertEquals(NotificationIcon.OTHER, icons[AncsCategory.OTHER.ordinal])
  }
}
