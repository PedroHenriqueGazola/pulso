package app.pulso.watch.notifications

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap

class AppAvatars(private val context: Context) {
  private val cache = mutableMapOf<AppAppearance, Bitmap>()

  fun avatarFor(appearance: AppAppearance): Bitmap = cache.getOrPut(appearance) { drawAvatar(appearance) }

  private fun drawAvatar(appearance: AppAppearance): Bitmap {
    val bitmap = createBitmap(AVATAR_SIZE_PIXELS, AVATAR_SIZE_PIXELS)
    val canvas = Canvas(bitmap)
    val backgroundColor = appearance.color ?: DEFAULT_BACKGROUND_COLOR
    val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = backgroundColor }
    val radius = AVATAR_SIZE_PIXELS / 2f
    canvas.drawCircle(radius, radius, radius, circlePaint)
    drawLogo(canvas, appearance.icon, logoColorOn(backgroundColor))
    return bitmap
  }

  private fun drawLogo(canvas: Canvas, icon: NotificationIcon, logoColor: Int) {
    val logo = ContextCompat.getDrawable(context, NotificationIcons.drawableFor(icon))?.mutate()
      ?: error("Ícone ausente para $icon")
    val inset = (AVATAR_SIZE_PIXELS * LOGO_INSET_RATIO).toInt()
    logo.setBounds(inset, inset, AVATAR_SIZE_PIXELS - inset, AVATAR_SIZE_PIXELS - inset)
    logo.setTint(logoColor)
    logo.draw(canvas)
  }

  private fun logoColorOn(backgroundColor: Int): Int {
    val isLightBackground = Color.luminance(backgroundColor) > LIGHT_BACKGROUND_LUMINANCE
    if (isLightBackground) return Color.BLACK
    return Color.WHITE
  }

  private companion object {
    const val AVATAR_SIZE_PIXELS = 144
    const val LOGO_INSET_RATIO = 0.22f
    const val LIGHT_BACKGROUND_LUMINANCE = 0.5f
    val DEFAULT_BACKGROUND_COLOR = Color.rgb(0x5F, 0x63, 0x68)
  }
}
