package guepardoapps.stopme.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Switch
import guepardoapps.stopme.R
import guepardoapps.stopme.common.Constants
import guepardoapps.stopme.controller.SharedPreferenceController
import guepardoapps.stopme.controller.SystemInfoController
import guepardoapps.stopme.service.FloatingService

class ActivitySettings : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.side_settings)

        val systemController = SystemInfoController(this)
        val sharedPreferenceController = SharedPreferenceController(this)

        val bubbleStateSwitch = findViewById<Switch>(R.id.switch_bubble_state)
        bubbleStateSwitch.isChecked = sharedPreferenceController.load(Constants.bubbleState, false) as Boolean
        bubbleStateSwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferenceController.save(Constants.bubbleState, isChecked)
            if (isChecked) {
                if (systemController.currentAndroidApi() >= android.os.Build.VERSION_CODES.M) {
                    systemController.checkAPI23SystemPermission(Constants.systemPermissionId)
                } else {
                    startService(Intent(this, FloatingService::class.java))
                }
            } else {
                if (systemController.isServiceRunning(FloatingService::class.java)) {
                    stopService(Intent(this, FloatingService::class.java))
                }
            }
        }
    }
}