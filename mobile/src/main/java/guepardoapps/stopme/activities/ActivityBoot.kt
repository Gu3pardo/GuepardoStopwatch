package guepardoapps.stopme.activities

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import com.github.guepardoapps.timext.kotlin.extensions.milliseconds
import com.github.guepardoapps.timext.kotlin.postDelayed
import guepardoapps.stopme.R
import guepardoapps.stopme.common.Constants
import guepardoapps.stopme.controller.SharedPreferenceController
import guepardoapps.stopme.controller.SystemInfoController
import guepardoapps.stopme.service.NavigationService

@ExperimentalUnsignedTypes
class ActivityBoot : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.side_boot)

        val sharedPreferenceController = SharedPreferenceController(this)

        if (!(sharedPreferenceController.load(Constants.sharedPrefName, false))) {
            sharedPreferenceController.run {
                save(Constants.bubbleState, true)
                save(Constants.bubblePosX, Constants.bubbleDefaultPosX)
                save(Constants.bubblePosY, Constants.bubbleDefaultPosY)
                save(Constants.sharedPrefName, true)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        val navigationService = NavigationService(this)
        val systemInfoController = SystemInfoController(this)

        if (systemInfoController.currentAndroidApi() >= android.os.Build.VERSION_CODES.M) {
            if (systemInfoController.checkAPI23SystemPermission(Constants.systemPermissionId)) {
                Handler().postDelayed({ navigationService.navigate(ActivityMain::class.java, true) }, resources.getInteger(R.integer.bootNavigationDelayInMs).milliseconds)
            }
        } else {
            Handler().postDelayed({ navigationService.navigate(ActivityMain::class.java, true) }, resources.getInteger(R.integer.bootNavigationDelayInMs).milliseconds)
        }
    }
}