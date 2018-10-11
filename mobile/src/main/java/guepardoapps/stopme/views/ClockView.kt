package guepardoapps.stopme.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView
import com.rey.material.widget.FloatingActionButton
import guepardoapps.stopme.R
import guepardoapps.stopme.activities.ActivityAbout
import guepardoapps.stopme.activities.ActivitySettings
import guepardoapps.stopme.extensions.integerFormat
import guepardoapps.stopme.logging.Logger
import guepardoapps.stopme.models.RxTime
import guepardoapps.stopme.service.ClockService
import guepardoapps.stopme.service.MailService
import guepardoapps.stopme.service.NavigationService

@SuppressLint("SetTextI18n")
class ClockView(context: Context, attributeSet: AttributeSet?) : RelativeLayout(context, attributeSet) {
    private val tag: String = ClockView::class.java.simpleName

    private val navigationService: NavigationService = NavigationService(context)

    private var textTimerMin: TextView
    private var textTimerSec: TextView
    private var textTimerMSec: TextView

    private var timeValue: TextView

    private var scrollView: ScrollView

    private var btnAbout: FloatingActionButton
    private var btnSettings: FloatingActionButton
    private var btnClose: FloatingActionButton

    private var btnClear: FloatingActionButton
    private var btnMail: FloatingActionButton

    private var btnStart: FloatingActionButton
    private var btnRound: FloatingActionButton
    private var btnStop: FloatingActionButton

    private lateinit var closeCallback: () -> Unit

    init {
        (context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(R.layout.clock_view, this)

        textTimerMin = findViewById(R.id.textTimerMin)
        textTimerSec = findViewById(R.id.textTimerSec)
        textTimerMSec = findViewById(R.id.textTimerMSec)

        timeValue = findViewById(R.id.timeValue)

        scrollView = findViewById(R.id.scrollView)

        btnAbout = findViewById(R.id.btnAbout)
        btnAbout.setOnClickListener { navigationService.navigate(ActivityAbout::class.java, false) }

        btnSettings = findViewById(R.id.btnSettings)
        btnSettings.setOnClickListener { navigationService.navigate(ActivitySettings::class.java, false) }

        btnClose = findViewById(R.id.btnClose)
        btnClose.setOnClickListener {
            try {
                closeCallback.invoke()
            } catch (exception: Exception) {
                Logger.instance.error(tag, exception)
            }
        }

        btnClear = findViewById(R.id.btnClear)
        btnClear.setOnClickListener { timeValue.text = "" }

        btnMail = findViewById(R.id.btnMail)
        btnMail.setOnClickListener { MailService(context).sendMail("Times", timeValue.text.toString(), arrayListOf(), true) }

        btnStart = findViewById(R.id.btnStart)
        btnStart.setOnClickListener {
            ClockService.instance.start()
            btnClear.visibility = View.INVISIBLE
        }

        btnRound = findViewById(R.id.btnRound)
        btnRound.setOnClickListener { ClockService.instance.round() }

        btnStop = findViewById(R.id.btnStop)
        btnStop.setOnClickListener {
            ClockService.instance.stop()

            val minutes = textTimerMin.text.toString()
            val seconds = textTimerSec.text.toString()
            val milliseconds = textTimerMSec.text.toString()
            timeValue.text = "________________________ \n\n${timeValue.text}\nTime = $minutes:$seconds:$milliseconds\n ________________________ \n\n"

            scrollView.fullScroll(View.FOCUS_DOWN)

            textTimerMin.setText(R.string.dummyTime)
            textTimerSec.setText(R.string.dummyTime)
            textTimerMSec.setText(R.string.dummyTime)

            btnClear.visibility = View.VISIBLE
        }
    }

    fun setCloseCallback(closeCallback: () -> Unit) {
        this.closeCallback = closeCallback
    }

    fun updateViews(rxTime: RxTime?) {
        if (rxTime === null || !rxTime.running) {
            btnClear.visibility = View.VISIBLE
            return
        }

        btnClear.visibility = View.INVISIBLE

        var finalSeconds = (rxTime.timeInMillisFinal / 1000).toInt()
        val finalMinutes = finalSeconds / 60
        finalSeconds %= 60
        val finalMilliSeconds = (rxTime.timeInMillisFinal % 1000).toInt()

        textTimerMin.text = finalMinutes.integerFormat(2)
        textTimerSec.text = finalSeconds.integerFormat(2)
        textTimerMSec.text = finalMilliSeconds.integerFormat(2)

        var btnExportText = ""
        rxTime.rounds.forEachIndexed { index, roundTimeInMillis -> btnExportText += createRoundText(index, roundTimeInMillis) }
        timeValue.text = btnExportText

        scrollView.fullScroll(View.FOCUS_DOWN)
    }

    private fun createRoundText(index: Int, roundTimeInMillis: Long): String {
        var roundSeconds = (roundTimeInMillis / 1000).toInt()
        val roundMinutes = roundSeconds / 60
        roundSeconds %= 60
        val roundMilliSeconds = (roundTimeInMillis % 1000).toInt()
        return "Round ${index + 1} = $roundMinutes:${roundSeconds.integerFormat(2)}:${roundMilliSeconds.integerFormat(3)}\n"
    }
}