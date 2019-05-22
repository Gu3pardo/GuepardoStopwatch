package guepardoapps.stopme.service

import android.os.Bundle

interface INavigationService {
    fun navigate(activity: Class<*>, finish: Boolean)

    fun navigateWithData(activity: Class<*>, data: Bundle, finish: Boolean)

    fun navigateToOtherApp(packageName: String, finish: Boolean): Boolean
}