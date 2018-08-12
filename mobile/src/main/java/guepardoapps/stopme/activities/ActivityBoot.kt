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

    private val navigationService: NavigationService = NavigationService(this)
    private val sharedPreferenceController: SharedPreferenceController = SharedPreferenceController(this)
    private val systemInfoController: SystemInfoController = SystemInfoController(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.side_boot)
        if (!(sharedPreferenceController.load(Constants.sharedPrefName, false) as Boolean)) {
            sharedPreferenceController.save(Constants.bubbleState, true)
            sharedPreferenceController.save(Constants.bubblePosY, Constants.bubbleDefaultPosY)
            sharedPreferenceController.save(Constants.sharedPrefName, true)
        }
    }

    override fun onResume() {
        super.onResume()
        if (systemInfoController.currentAndroidApi() >= android.os.Build.VERSION_CODES.M) {
            if (systemInfoController.checkAPI23SystemPermission(Constants.systemPermissionId)) {
                Handler().postDelayed({ navigationService.navigate(ActivityMain::class.java, true) }, 1500)
            }
        } else {
            Handler().postDelayed({ navigationService.navigate(ActivityMain::class.java, true) }, 1500)
        }
    }
}