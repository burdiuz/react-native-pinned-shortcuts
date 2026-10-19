package com.actualwave.pinnedshortcuts

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.net.Uri
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod

/**
 * Home-screen shortcuts that open the app on a deep link (e.g. `myapp://run?file=…`).
 * Re-pinning an existing id updates that shortcut instead of adding a second one.
 */
class PinnedShortcutsModule(private val context: ReactApplicationContext) :
  ReactContextBaseJavaModule(context) {
  override fun getName() = "ActualwavePinnedShortcuts"

  @ReactMethod(isBlockingSynchronousMethod = true)
  fun isSupported(): Boolean = ShortcutManagerCompat.isRequestPinShortcutSupported(context)

  // Resolves true when the launcher accepted the request (the user still has to confirm it).
  // `iconPath` is an optional local image (file:// URI or path); it is centred on a solid
  // background inside the adaptive-icon safe zone. Without it the app icon is used.
  @ReactMethod
  fun requestPin(id: String, label: String, url: String, iconPath: String?, background: String?, promise: Promise) {
    try {
      val icon = iconPath?.let { composeIcon(it, background) }
        ?.let { IconCompat.createWithAdaptiveBitmap(it) }
        ?: IconCompat.createWithResource(context, context.applicationInfo.icon)
      val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).setPackage(context.packageName)
      val info = ShortcutInfoCompat.Builder(context, id)
        .setShortLabel(label)
        .setLongLabel(label)
        .setIcon(icon)
        .setIntent(intent)
        .build()
      promise.resolve(ShortcutManagerCompat.requestPinShortcut(context, info, null))
    } catch (e: Exception) {
      promise.reject("ERR_PIN_SHORTCUT", e.message, e)
    }
  }

  private fun composeIcon(path: String, background: String?): Bitmap? {
    val src = BitmapFactory.decodeFile(path.removePrefix("file://")) ?: return null
    val size = 432 // 108dp adaptive layer at xxxhdpi
    val out = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(out)
    canvas.drawColor(try { Color.parseColor(background ?: "#1F2937") } catch (e: IllegalArgumentException) { Color.DKGRAY })
    // Adaptive icons show the centre 66%; keep the picture inside that.
    val box = (size * 0.56f).toInt()
    val scale = minOf(box.toFloat() / src.width, box.toFloat() / src.height)
    val w = (src.width * scale).toInt()
    val h = (src.height * scale).toInt()
    val left = (size - w) / 2
    val top = (size - h) / 2
    canvas.drawBitmap(src, null, Rect(left, top, left + w, top + h), null)
    return out
  }
}
