package guepardoapps.stopme.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import guepardoapps.stopme.R
import guepardoapps.stopme.common.Constants
import guepardoapps.stopme.controller.SharedPreferenceController
import guepardoapps.stopme.controller.SystemInfoController
import guepardoapps.stopme.logging.Logger
import guepardoapps.stopme.service.ClockService
import guepardoapps.stopme.service.FloatingService
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.side_main.*

class ActivityMain : Activity() {
    private val tag: String = ActivityMain::class.java.simpleName

    private lateinit var subscription: Disposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.side_main)

        clockView.setCloseCallback { finish() }

        if (ClockService.instance.isDisposed) {
            ClockService.instance.initialize(this)
        }

        subscription = ClockService.instance.timePublishSubject
                .subscribeOn(Schedulers.io())
                .subscribe(
                        { response -> clockView.updateViews(response) },
                        { error -> Logger.instance.error(tag, error) })
    }

    public override fun onResume() {
        super.onResume()
    }

    public override fun onPause() {
        super.onPause()
        tryToStartService()
    }

    public override fun onDestroy() {
        super.onDestroy()
        tryToStartService()
        subscription.dispose()
    }

    private fun tryToStartService() {
        if (!SystemInfoController(this).isServiceRunning(FloatingService::class.java)
                && SharedPreferenceController(this).load(Constants.bubbleState, false) as Boolean) {
            startService(Intent(this, FloatingService::class.java))
        }
    }
}