package guepardoapps.stopme.service

interface IMailService {
    fun sendMail(subject: String, text: String, addresses: ArrayList<String>, startNewActivity: Boolean)
}