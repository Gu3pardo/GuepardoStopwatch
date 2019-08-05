package guepardoapps.stopme.controller

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.NonNull
import android.view.Display
import android.view.WindowManager

internal class SystemInfoController(@NonNull private val context: Context) : ISystemInfoController {

    override fun canDrawOverlay(): Boolean = Settings.canDrawOverlays(context)

    override fun checkAPI23SystemPermission(permissionRequestId: Int): Boolean {
        if (!Settings.canDrawOverlays(context)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + context.packageName))
            (context as Activity).startActivityForResult(intent, permissionRequestId)
            return false
        }
        return true
    }

    override fun currentAndroidApi(): Int = Build.VERSION.SDK_INT

    override fun displayDimension(): Display = (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay

    @Suppress("DEPRECATION")
    override fun isServiceRunning(serviceClassName: String): Boolean =
            (context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
                    .getRunningServices(Integer.MAX_VALUE)!!
                    .any { serviceInfo -> serviceClassName == serviceInfo.service.className }

    override fun isServiceRunning(serviceClass: Class<*>): Boolean = isServiceRunning(serviceClass.name)
}