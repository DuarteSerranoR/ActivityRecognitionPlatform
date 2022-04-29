package pie.activityrecognition.platform.android

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager

class SMSActivity(c: Context) {

    private val context = c
    private val smsManager = context.getSystemService(SmsManager::class.java)

    fun sendSMS(phoneNumber: String, message: String) {
        val sentPI: PendingIntent = PendingIntent.getBroadcast(this.context, 0, Intent("SMS_SENT"), 0)
        this.smsManager.sendTextMessage(phoneNumber, null, message, sentPI, null)
        //this.smsManager.sendTextMessage(phoneNumber, null, message, null, null)
    }
}
