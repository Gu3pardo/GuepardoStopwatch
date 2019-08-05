package guepardoapps.stopme.controller

import android.os.Bundle

internal interface INavigationController {
    fun navigate(activity: Class<*>, finish: Boolean)

    fun navigateToOtherApp(packageName: String, finish: Boolean): Boolean

    fun navigateWithData(activity: Class<*>, data: Bundle, finish: Boolean)
}