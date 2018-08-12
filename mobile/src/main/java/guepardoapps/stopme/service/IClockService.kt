package guepardoapps.stopme.service

import android.content.Context
import guepardoapps.stopme.models.RxTime
import io.reactivex.subjects.PublishSubject

interface IClockService {
    val timePublishSubject: PublishSubject<RxTime>
    fun initialize(context: Context)
    fun start()
    fun pause()
    fun stop()
}