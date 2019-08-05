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
import guepardoapps.stopme.controller.MailController
import guepardoapps.stopme.controller.NavigationController
import guepardoapps.stopme.extensions.integerFormat
import guepardoapps.stopme.logging.Logger
import guepardoapps.stopme.models.RxTime
import guepardoapps.stopme.service.ClockService

@ExperimentalUnsignedTypes
@SuppressLint("SetTextI18n")
class ClockView(context: Context, attributeSet: AttributeSet?) : RelativeLayout(context, attributeSet) {

    private var btnClear: FloatingActionButton

    private val navigationController: NavigationController = NavigationController(context)

    private var scrollView: ScrollView

    private var textTimerMin: TextView

    private var textTimerMSec: TextView

    private var textTimerSec: TextView

    private var timeValue: TextView

    private lateinit var closeCallback: () -> Unit

    init {
        (context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(R.layout.clock_view, this)

        textTimerMin = findViewById(R.id.textTimerMin)
        textTimerSec = findViewById(R.id.textTimerSec)
        textTimerMSec = findViewById(R.id.textTimerMSec)
        timeValue = findViewById(R.id.timeValue)
        scrollView = findViewById(R.id.scrollView)
        btnClear = findViewById(R.id.btnClear)

        btnClear.setOnClickListener { timeValue.text = "" }

        findViewById<FloatingActionButton>(R.id.btnAbout).setOnClickListener { navigationController.navigate(ActivityAbout::class.java, false) }
        findViewById<FloatingActionButton>(R.id.btnSettings).setOnClickListener { navigationController.navigate(ActivitySettings::class.java, false) }
        findViewById<FloatingActionButton>(R.id.btnClose).setOnClickListener {
            try {
                closeCallback.invoke()
            } catch (exception: Exception) {
                Logger.instance.error(ClockView::class.java.simpleName, exception)
            }
        }
        findViewById<FloatingActionButton>(R.id.btnMail).setOnClickListener { MailController(context).sendMail("Times", timeValue.text.toString(), arrayListOf(), true) }
        findViewById<FloatingActionButton>(R.id.btnStart).setOnClickListener {
            ClockService.instance.start()
            btnClear.visibility = View.INVISIBLE
        }
        findViewById<FloatingActionButton>(R.id.btnRound).setOnClickListener { ClockService.instance.round() }
        findViewById<FloatingActionButton>(R.id.btnStop).setOnClickListener {
            ClockService.instance.stop()
            timeValue.text = "________________________ \n\n${timeValue.text}\nTime = ${textTimerMin.text}:${textTimerSec.text}:${textTimerMSec.text}\n ________________________ \n\n"
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
        val finalMillis = (rxTime.timeInMillisFinal % 1000).toInt()

        textTimerMin.text = finalMinutes.integerFormat(2)
        textTimerSec.text = finalSeconds.integerFormat(2)
        textTimerMSec.text = finalMillis.integerFormat(2)

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