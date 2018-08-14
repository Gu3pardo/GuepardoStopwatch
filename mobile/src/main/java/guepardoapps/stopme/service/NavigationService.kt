package guepardoapps.stopme.service

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle

class NavigationService(val context: Context) : INavigationService {
    override fun navigate(activity: Class<*>, finish: Boolean) {
        context.startActivity(Intent(context, activity))
        if (finish) {
            (context as Activity).finish()
        }
    }

    override fun navigateWithData(activity: Class<*>, data: Bundle, finish: Boolean) {
        val navigateIntent = Intent(context, activity)
        navigateIntent.putExtras(data)
        context.startActivity(navigateIntent)
        if (finish) {
            (context as Activity).finish()
        }
    }

    override fun navigateToOtherApp(packageName: String, finish: Boolean): Boolean {
        val packageManager = context.packageManager
        try {
            val startAppIntent = packageManager.getLaunchIntentForPackage(packageName)
                    ?: return false

            startAppIntent.addCategory(Intent.CATEGORY_LAUNCHER)
            context.startActivity(startAppIntent)

            if (finish) {
                (context as Activity).finish()
            }

            return true
        } catch (e: Exception) {
            return false
        }
    }
}