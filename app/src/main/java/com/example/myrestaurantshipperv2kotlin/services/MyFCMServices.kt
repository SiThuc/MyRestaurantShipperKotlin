package com.example.myrestaurantshipperv2kotlin.services

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.myrestaurantshipperv2kotlin.common.Common
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.*

class MyFCMServices : FirebaseMessagingService() {

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        Common.updateToken(this, p0, false, true)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val dataRecv = message.data
        if(dataRecv != null){
            Common.showNotification(this, Random().nextInt(),
                dataRecv[Common.NOTI_TITLE],
                dataRecv[Common.NOTI_CONTENT],
                null)
        }
    }
}