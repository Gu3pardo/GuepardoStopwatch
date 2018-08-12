package guepardoapps.stopme.controller

interface IMailController {
    fun sendMail(subject: String = "", text: String = "", address: String = "", startNewActivity: Boolean = false)
}