package app.pulso.watch.notifications

import androidx.annotation.DrawableRes
import app.pulso.watch.R

object NotificationIcons {
  @DrawableRes
  fun drawableFor(icon: NotificationIcon): Int = when (icon) {
    NotificationIcon.WHATSAPP -> R.drawable.ic_app_whatsapp
    NotificationIcon.INSTAGRAM -> R.drawable.ic_app_instagram
    NotificationIcon.GMAIL -> R.drawable.ic_app_gmail
    NotificationIcon.TELEGRAM -> R.drawable.ic_app_telegram
    NotificationIcon.NUBANK -> R.drawable.ic_app_nubank
    NotificationIcon.IFOOD -> R.drawable.ic_app_ifood
    NotificationIcon.UBER -> R.drawable.ic_app_uber
    NotificationIcon.SHOPEE -> R.drawable.ic_app_shopee
    NotificationIcon.MESSAGES -> R.drawable.ic_app_messages
    NotificationIcon.TEAM_CHAT -> R.drawable.ic_app_team_chat
    NotificationIcon.SHOPPING -> R.drawable.ic_app_shopping
    NotificationIcon.MESSAGE -> R.drawable.ic_category_message
    NotificationIcon.EMAIL -> R.drawable.ic_category_email
    NotificationIcon.SCHEDULE -> R.drawable.ic_category_schedule
    NotificationIcon.CALL -> R.drawable.ic_category_call
    NotificationIcon.MISSED_CALL -> R.drawable.ic_category_missed_call
    NotificationIcon.VOICEMAIL -> R.drawable.ic_category_voicemail
    NotificationIcon.NEWS -> R.drawable.ic_category_news
    NotificationIcon.HEALTH -> R.drawable.ic_category_health
    NotificationIcon.FINANCE -> R.drawable.ic_category_finance
    NotificationIcon.LOCATION -> R.drawable.ic_category_location
    NotificationIcon.ENTERTAINMENT -> R.drawable.ic_category_entertainment
    NotificationIcon.SOCIAL -> R.drawable.ic_category_social
    NotificationIcon.OTHER -> R.drawable.ic_category_other
  }
}
