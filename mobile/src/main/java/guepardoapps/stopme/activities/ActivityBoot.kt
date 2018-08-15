package guepardoapps.stopme.activities

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import guepardoapps.stopme.R
import guepardoapps.stopme.common.Constants
import guepardoapps.stopme.controller.SharedPreferenceController
import guepardoapps.stopme.controller.SystemInfoController
import guepardoapps.stopme.service.NavigationService

class ActivityBoot : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.side_boot)

        val sharedPreferenceController = SharedPreferenceController(this)

        if (!(sharedPreferenceController.load(Constants.sharedPrefName, false) as Boolean)) {
            sharedPreferenceController.save(Constants.bubbleState, true)
            sharedPreferenceController.save(Constants.bubblePosX, Constants.bubbleDefaultPosX)
            sharedPreferenceController.save(Constants.bubblePosY, Constants.bubbleDefaultPosY)
            sharedPreferenceController.save(Constants.sharedPrefName, true)
        }
    }

    override fun onResume() {
        super.onResume()

        val navigationService = NavigationService(this)
        val systemInfoController = SystemInfoController(this)

        if (systemInfoController.currentAndroidApi() >= android.os.Build.VERSION_CODES.M) {
            if (systemInfoController.checkAPI23SystemPermission(Constants.systemPermissionId)) {
                Handler().postDelayed({ navigationService.navigate(ActivityMain::class.java, true) }, 1500)
            }
        } else {
            Handler().postDelayed({ navigationService.navigate(ActivityMain::class.java, true) }, 1500)
        }
    }
}