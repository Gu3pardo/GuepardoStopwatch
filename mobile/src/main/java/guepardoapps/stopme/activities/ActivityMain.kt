package guepardoapps.stopme.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ScrollView
import android.widget.TextView
import com.rey.material.widget.FloatingActionButton
import guepardoapps.stopme.R
import guepardoapps.stopme.common.Constants
import guepardoapps.stopme.controller.SharedPreferenceController
import guepardoapps.stopme.controller.SystemInfoController
import guepardoapps.stopme.extensions.integerFormat
import guepardoapps.stopme.models.RxTime
import guepardoapps.stopme.service.ClockService
import guepardoapps.stopme.service.FloatingService
import guepardoapps.stopme.service.MailService
import guepardoapps.stopme.service.NavigationService
import guepardoapps.stopme.utils.Logger
import io.reactivex.schedulers.Schedulers

class ActivityMain : Activity() {
    private val tag: String = ActivityMain::class.java.simpleName

    private lateinit var navigationService: NavigationService

    private var minuteView: TextView? = null
    private var secondsView: TextView? = null
    private var milliSecondsView: TextView? = null
    private var scrollView: ScrollView? = null
    private var btnAbout: FloatingActionButton? = null
    private var btnSettings: FloatingActionButton? = null
    private var btnClose: FloatingActionButton? = null
    private var btnClear: FloatingActionButton? = null
    private var btnMail: FloatingActionButton? = null
    private var btnStart: FloatingActionButton? = null
    private var btnRound: FloatingActionButton? = null
    private var btnStop: FloatingActionButton? = null
    private var timeTextView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.side_main)

        navigationService = NavigationService(this)

        findViews()
        addActionsToViews()
        checkBtnClear()

        if (ClockService.instance.isDisposed) {
            ClockService.instance.initialize(this)
        }

        ClockService.instance.timePublishSubject
                .subscribeOn(Schedulers.io())
                .subscribe(
                        { response -> updateViews(response) },
                        { error -> Logger.instance.error(tag, error) })
    }

    public override fun onResume() {
        super.onResume()
        checkBtnClear()
    }

    public override fun onPause() {
        super.onPause()
        tryToStartService()
    }

    public override fun onDestroy() {
        super.onDestroy()
        tryToStartService()
    }

    private fun findViews() {
        minuteView = findViewById(R.id.textTimerMin)
        secondsView = findViewById(R.id.textTimerSec)
        milliSecondsView = findViewById(R.id.textTimerMsec)

        scrollView = findViewById(R.id.scrollView)

        btnAbout = findViewById(R.id.btnAbout)
        btnSettings = findViewById(R.id.btnSettings)
        btnClose = findViewById(R.id.btnClose)
        btnClear = findViewById(R.id.btnClear)
        btnMail = findViewById(R.id.btnMail)
        btnStart = findViewById(R.id.btnStart)
        btnRound = findViewById(R.id.btnRound)
        btnStop = findViewById(R.id.btnStop)
        timeTextView = findViewById(R.id.timeValue)
    }

    @SuppressLint("SetTextI18n")
    private fun addActionsToViews() {
        btnAbout?.setOnClickListener { navigationService.navigate(ActivityAbout::class.java, false) }
        btnSettings?.setOnClickListener { navigationService.navigate(ActivitySettings::class.java, false) }
        btnClose?.setOnClickListener { finish() }
        btnClear?.setOnClickListener { timeTextView?.text = "" }
        btnMail?.setOnClickListener { MailService(this).sendMail("Times", timeTextView?.text.toString(), arrayListOf(), true) }
        btnStart?.setOnClickListener { ClockService.instance.start(); btnClear?.visibility = View.INVISIBLE; }
        btnRound?.setOnClickListener { ClockService.instance.round() }
        btnStop?.setOnClickListener {
            ClockService.instance.stop()

            val minutes = minuteView?.text.toString()
            val seconds = secondsView?.text.toString()
            val milliseconds = milliSecondsView?.text.toString()
            timeTextView?.text = "________________________ \n\n${timeTextView?.text}\nTime = $minutes:$seconds:$milliseconds\n ________________________ \n\n"

            scrollView?.fullScroll(View.FOCUS_DOWN)

            minuteView?.setText(R.string.dummyTime)
            secondsView?.setText(R.string.dummyTime)
            milliSecondsView?.setText(R.string.dummyTime)

            btnClear?.visibility = View.VISIBLE
        }
    }

    private fun updateViews(rxTime: RxTime?) {
        if (rxTime === null || !rxTime.running) {
            return
        }

        var finalSeconds = (rxTime.timeInMillisFinal / 1000).toInt()
        val finalMinutes = finalSeconds / 60
        finalSeconds %= 60
        val finalMilliSeconds = (rxTime.timeInMillisFinal % 1000).toInt()

        minuteView?.text = finalMinutes.integerFormat(2)
        secondsView?.text = finalSeconds.integerFormat(2)
        milliSecondsView?.text = finalMilliSeconds.integerFormat(2)

        var btnExportText = ""
        rxTime.rounds.forEachIndexed { index, roundTimeInMillis -> btnExportText += createRoundText(index, roundTimeInMillis) }
        timeTextView?.text = btnExportText

        scrollView?.fullScroll(View.FOCUS_DOWN)
    }

    private fun createRoundText(index: Int, roundTimeInMillis: Long): String {
        var roundSeconds = (roundTimeInMillis / 1000).toInt()
        val roundMinutes = roundSeconds / 60
        roundSeconds %= 60
        val roundMilliSeconds = (roundTimeInMillis % 1000).toInt()
        return "Round ${index + 1} = $roundMinutes:${roundSeconds.integerFormat(2)}:${roundMilliSeconds.integerFormat(3)}\n"
    }

    private fun tryToStartService() {
        if (!SystemInfoController(this).isServiceRunning(FloatingService::class.java)
                && SharedPreferenceController(this).load(Constants.bubbleState, false) as Boolean) {
            startService(Intent(this, FloatingService::class.java))
        }
    }

    private fun checkBtnClear() {
        if (ClockService.instance.isRunning) {
            btnClear?.visibility = View.INVISIBLE
        } else {
            btnClear?.visibility = View.VISIBLE
        }
    }
}