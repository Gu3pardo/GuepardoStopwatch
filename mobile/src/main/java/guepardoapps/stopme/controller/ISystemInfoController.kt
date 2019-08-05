package guepardoapps.stopme.controller

import android.view.Display

internal interface ISystemInfoController {
    fun canDrawOverlay(): Boolean

    fun checkAPI23SystemPermission(permissionRequestId: Int): Boolean

    fun currentAndroidApi(): Int

    fun displayDimension(): Display

    fun isServiceRunning(serviceClassName: String): Boolean

    fun isServiceRunning(serviceClass: Class<*>): Boolean
}