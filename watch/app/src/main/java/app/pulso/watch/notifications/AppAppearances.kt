package app.pulso.watch.notifications

import app.pulso.watch.ancs.AncsCategory

enum class NotificationIcon {
  WHATSAPP,
  INSTAGRAM,
  GMAIL,
  TELEGRAM,
  NUBANK,
  IFOOD,
  UBER,
  SHOPEE,
  MESSAGES,
  TEAM_CHAT,
  SHOPPING,
  MESSAGE,
  EMAIL,
  SCHEDULE,
  CALL,
  MISSED_CALL,
  VOICEMAIL,
  NEWS,
  HEALTH,
  FINANCE,
  LOCATION,
  ENTERTAINMENT,
  SOCIAL,
  OTHER,
}

data class AppAppearance(val label: String, val icon: NotificationIcon, val color: Int?)

object AppAppearances {
  private const val APPLE_GREEN = 0xFF34C759.toInt()
  private const val APPLE_BLUE = 0xFF1A8CFF.toInt()
  private const val APPLE_RED = 0xFFFF3B30.toInt()
  private const val APPLE_ORANGE = 0xFFFF9500.toInt()
  private const val GOOGLE_BLUE = 0xFF4285F4.toInt()

  private val KNOWN_APPS = mapOf(
    "net.whatsapp.WhatsApp" to AppAppearance("WhatsApp", NotificationIcon.WHATSAPP, 0xFF25D366.toInt()),
    "net.whatsapp.WhatsAppSMB" to AppAppearance("WhatsApp Business", NotificationIcon.WHATSAPP, 0xFF25D366.toInt()),
    "com.apple.MobileSMS" to AppAppearance("Mensagens", NotificationIcon.MESSAGES, APPLE_GREEN),
    "com.apple.mobilephone" to AppAppearance("Telefone", NotificationIcon.CALL, APPLE_GREEN),
    "com.apple.facetime" to AppAppearance("FaceTime", NotificationIcon.CALL, APPLE_GREEN),
    "com.apple.mobilemail" to AppAppearance("Mail", NotificationIcon.EMAIL, APPLE_BLUE),
    "com.apple.mobilecal" to AppAppearance("Calendário", NotificationIcon.SCHEDULE, APPLE_RED),
    "com.apple.reminders" to AppAppearance("Lembretes", NotificationIcon.SCHEDULE, APPLE_ORANGE),
    "com.google.Gmail" to AppAppearance("Gmail", NotificationIcon.GMAIL, 0xFFEA4335.toInt()),
    "com.google.calendar" to AppAppearance("Google Agenda", NotificationIcon.SCHEDULE, GOOGLE_BLUE),
    "com.google.hangouts" to AppAppearance("Google Chat", NotificationIcon.TEAM_CHAT, 0xFF00AC47.toInt()),
    "com.google.chat" to AppAppearance("Google Chat", NotificationIcon.TEAM_CHAT, 0xFF00AC47.toInt()),
    "ph.telegra.Telegraph" to AppAppearance("Telegram", NotificationIcon.TELEGRAM, 0xFF26A5E4.toInt()),
    "org.telegram.Telegram" to AppAppearance("Telegram", NotificationIcon.TELEGRAM, 0xFF26A5E4.toInt()),
    "com.burbn.instagram" to AppAppearance("Instagram", NotificationIcon.INSTAGRAM, 0xFFFF0069.toInt()),
    "com.facebook.Messenger" to AppAppearance("Messenger", NotificationIcon.MESSAGE, 0xFF0084FF.toInt()),
    "com.tinyspeck.chatlyio" to AppAppearance("Slack", NotificationIcon.TEAM_CHAT, 0xFFE01E5A.toInt()),
    "com.microsoft.skype.teams" to AppAppearance("Teams", NotificationIcon.TEAM_CHAT, 0xFF7B83EB.toInt()),
    "com.nu.iphone" to AppAppearance("Nubank", NotificationIcon.NUBANK, 0xFF820AD1.toInt()),
    "com.ubercab.UberClient" to AppAppearance("Uber", NotificationIcon.UBER, 0xFFFFFFFF.toInt()),
    "br.com.brainweb.ifood" to AppAppearance("iFood", NotificationIcon.IFOOD, 0xFFEA1D2C.toInt()),
    "com.beeasy.shopee.br" to AppAppearance("Shopee", NotificationIcon.SHOPEE, 0xFFEE4D2D.toInt()),
    "com.3mosquitos.MercadoLibre" to AppAppearance("Mercado Livre", NotificationIcon.SHOPPING, 0xFFFFE600.toInt()),
  )

  private val CATEGORY_ICONS = mapOf(
    AncsCategory.OTHER to NotificationIcon.OTHER,
    AncsCategory.INCOMING_CALL to NotificationIcon.CALL,
    AncsCategory.MISSED_CALL to NotificationIcon.MISSED_CALL,
    AncsCategory.VOICEMAIL to NotificationIcon.VOICEMAIL,
    AncsCategory.SOCIAL to NotificationIcon.SOCIAL,
    AncsCategory.SCHEDULE to NotificationIcon.SCHEDULE,
    AncsCategory.EMAIL to NotificationIcon.EMAIL,
    AncsCategory.NEWS to NotificationIcon.NEWS,
    AncsCategory.HEALTH_AND_FITNESS to NotificationIcon.HEALTH,
    AncsCategory.BUSINESS_AND_FINANCE to NotificationIcon.FINANCE,
    AncsCategory.LOCATION to NotificationIcon.LOCATION,
    AncsCategory.ENTERTAINMENT to NotificationIcon.ENTERTAINMENT,
  )

  fun appearanceFor(appIdentifier: String, category: AncsCategory): AppAppearance =
    KNOWN_APPS[appIdentifier] ?: buildUnknownAppAppearance(appIdentifier, category)

  private fun buildUnknownAppAppearance(appIdentifier: String, category: AncsCategory) = AppAppearance(
    label = appIdentifier.substringAfterLast('.').ifEmpty { "iPhone" },
    icon = CATEGORY_ICONS.getValue(category),
    color = null,
  )
}
