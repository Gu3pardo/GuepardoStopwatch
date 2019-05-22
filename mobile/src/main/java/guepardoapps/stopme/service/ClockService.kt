package guepardoapps.stopme.service

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.SystemClock
import guepardoapps.stopme.extensions.replaceLast
import guepardoapps.stopme.models.RxTime
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject

class ClockService private constructor() : IClockService, Disposable {

    private var context: Context? = null

    private var stopwatchHandler: Handler = Handler()
    private var running: Boolean = false
    private var startTimeFinal = 0L
    private var roundStartTime = 0L
    private var roundList: ArrayList<Long> = arrayListOf()

    private val updateTimerMethod = object : Runnable {
        override fun run() {
            val timeInMillisFinal = SystemClock.uptimeMillis() - startTimeFinal
            val roundTimeInMillis = SystemClock.uptimeMillis() - roundStartTime
            roundList.replaceLast(roundTimeInMillis)

            timePublishSubject.onNext(RxTime(running, timeInMillisFinal, roundList))

            stopwatchHandler.postDelayed(this, 1)
        }
    }

    override val timePublishSubject: PublishSubject<RxTime> = PublishSubject.create<RxTime>()

    private object Holder {
        @SuppressLint("StaticFieldLeak")
        val instance: ClockService = ClockService()
    }

    companion object {
        val instance: ClockService by lazy { Holder.instance }
    }

    override fun initialize(context: Context) {
        if (this.context === null) {
            this.context = context
        }
    }

    override fun start() {
        if (!running) {
            startTimeFinal = SystemClock.uptimeMillis()
            roundStartTime = SystemClock.uptimeMillis()
            roundList = arrayListOf(0)

            stopwatchHandler.postDelayed(updateTimerMethod, 1)
            running = true

            timePublishSubject.onNext(RxTime(running, 0, roundList))
        }
    }

    override fun round() {
        if (running) {
            val roundTimeInMillis = SystemClock.uptimeMillis() - roundStartTime
            roundList.add(roundTimeInMillis)
            roundStartTime = SystemClock.uptimeMillis()
        }
    }

    override fun stop() {
        if (running) {
            val timeInMillisFinal = SystemClock.uptimeMillis() - startTimeFinal
            val roundTimeInMillis = SystemClock.uptimeMillis() - roundStartTime
            roundList.replaceLast(roundTimeInMillis)

            startTimeFinal = 0L
            roundStartTime = 0L

            stopwatchHandler.removeCallbacks(updateTimerMethod)
            running = false

            timePublishSubject.onNext(RxTime(running, timeInMillisFinal, roundList))
        }
    }

    override fun isDisposed(): Boolean = this.context === null

    override fun dispose() {
        this.stopwatchHandler.removeCallbacks(updateTimerMethod)
        this.context = null
    }
}