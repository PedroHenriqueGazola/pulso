package app.pulso.watch.notifications

object AppLabels {
  private val KNOWN_APPS = mapOf(
    "net.whatsapp.WhatsApp" to "WhatsApp",
    "net.whatsapp.WhatsAppSMB" to "WhatsApp Business",
    "com.apple.MobileSMS" to "Mensagens",
    "com.apple.mobilephone" to "Telefone",
    "com.apple.facetime" to "FaceTime",
    "com.apple.mobilemail" to "Mail",
    "com.apple.mobilecal" to "Calendário",
    "com.apple.reminders" to "Lembretes",
    "com.google.Gmail" to "Gmail",
    "com.google.calendar" to "Google Agenda",
    "com.google.hangouts" to "Google Chat",
    "com.google.chat" to "Google Chat",
    "ph.telegra.Telegraph" to "Telegram",
    "org.telegram.Telegram" to "Telegram",
    "com.burbn.instagram" to "Instagram",
    "com.facebook.Messenger" to "Messenger",
    "com.tinyspeck.chatlyio" to "Slack",
    "com.microsoft.skype.teams" to "Teams",
    "com.nubank.nubank" to "Nubank",
    "com.ubercab.UberClient" to "Uber",
    "com.ifood.ifood" to "iFood",
  )

  fun labelFor(appIdentifier: String): String =
    KNOWN_APPS[appIdentifier] ?: appIdentifier.substringAfterLast('.').ifEmpty { "iPhone" }
}
