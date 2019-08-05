package guepardoapps.stopme.activities

// Library used:
// https://github.com/daniel-stoneuk/material-about-library

// Icons from:
// https://icons8.de/

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.danielstone.materialaboutlibrary.MaterialAboutActivity
import com.danielstone.materialaboutlibrary.items.MaterialAboutActionItem
import com.danielstone.materialaboutlibrary.model.MaterialAboutCard
import com.danielstone.materialaboutlibrary.model.MaterialAboutList
import guepardoapps.stopme.R
import guepardoapps.stopme.controller.MailController

class ActivityAbout : MaterialAboutActivity() {

    override fun getActivityTitle(): CharSequence? = getString(R.string.about)

    override fun getMaterialAboutList(context: Context): MaterialAboutList {
        val infoCard: MaterialAboutCard = MaterialAboutCard.Builder()
                .addItem(
                        MaterialAboutActionItem.Builder()
                                .text(R.string.app_name)
                                .subText(R.string.copyright)
                                .icon(android.R.drawable.ic_dialog_info)
                                .build())
                .addItem(
                        MaterialAboutActionItem.Builder()
                                .text(R.string.author_title)
                                .subText(R.string.author)
                                .icon(R.drawable.about)
                                .build())
                .addItem(
                        MaterialAboutActionItem.Builder()
                                .text(R.string.version_title)
                                .subText(R.string.version)
                                .icon(android.R.drawable.ic_menu_info_details)
                                .build())
                .build()

        val mailCard: MaterialAboutCard = MaterialAboutCard.Builder()
                .title(R.string.email_title)
                .addItem(MaterialAboutActionItem.Builder()
                        .text(R.string.email)
                        .icon(R.mipmap.mail)
                        .setOnClickAction { MailController(this).sendMail("", "", arrayListOf(getString(R.string.email)), true) }
                        .build())
                .build()

        val homePageCard: MaterialAboutCard = MaterialAboutCard.Builder()
                .title(R.string.homepageLink_title)
                .addItem(MaterialAboutActionItem.Builder()
                        .text(R.string.homepageLink)
                        .icon(R.mipmap.external_link)
                        .setOnClickAction { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.homepageLink)))) }
                        .build())
                .build()

        val gitHubCard: MaterialAboutCard = MaterialAboutCard.Builder()
                .title(R.string.gitHubLink_title)
                .addItem(MaterialAboutActionItem.Builder()
                        .text(R.string.gitHubLink)
                        .icon(R.mipmap.github)
                        .setOnClickAction { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.gitHubLink)))) }
                        .build())
                .build()

        return MaterialAboutList.Builder()
                .addCard(infoCard)
                .addCard(mailCard)
                .addCard(homePageCard)
                .addCard(gitHubCard)
                .build()
    }
}