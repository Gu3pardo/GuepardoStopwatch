package guepardoapps.stopme.controller

import android.content.Context
import android.content.Intent

internal class MailController(val context: Context) : IMailController {
    override fun sendMail(subject: String, text: String, addresses: ArrayList<String>, startNewActivity: Boolean) {
        val mailIntent = Intent(Intent.ACTION_SEND)

        if (startNewActivity) {
            mailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        if (!subject.isBlank()) {
            mailIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
        }

        if (!text.isBlank()) {
            mailIntent.putExtra(Intent.EXTRA_TEXT, text)
        }

        if (addresses.size > 0) {
            mailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(addresses))
            mailIntent.type = "message/rfc822"
        } else {
            mailIntent.type = "plain/text"
        }

        context.startActivity(Intent.createChooser(mailIntent, "Send mail..."))
    }
}