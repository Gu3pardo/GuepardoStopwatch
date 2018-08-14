package guepardoapps.stopme.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import guepardoapps.stopme.R
import guepardoapps.stopme.service.MailService

@Suppress("UNUSED_PARAMETER")
class ActivityAbout : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.side_about)
    }

    fun sendMail(view: View) {
        MailService(this).sendMail("", "", arrayListOf(getString(R.string.email)), true)
    }

    fun goToHomepage(view: View) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.homepageLink))))
    }

    fun goToGitHub(view: View) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.gitHubLink))))
    }

    fun payPal(view: View) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.payPalLink))))
    }
}