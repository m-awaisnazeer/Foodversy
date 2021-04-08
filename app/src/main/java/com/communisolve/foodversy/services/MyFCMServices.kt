package com.communisolve.foodversy.services

import com.communisolve.foodversy.common.Common
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random

class MyFCMServices : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Common.updateToken(this,token)
    }


    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        if (remoteMessage.data !=null && !remoteMessage.data.isEmpty()){
            val dataRecv = remoteMessage.data
            if (!dataRecv.isEmpty()){
                Common.showNotification(this, Random.nextInt(),
                    dataRecv[Common.NOTI_TITLE]+"data..",
                    dataRecv[Common.NOTI_CONTENT]+"data..",
                    null)
            }
        }
        if (remoteMessage.notification !=null){
            val dataRecv = remoteMessage.notification

            Common.showNotification(this, Random.nextInt(),
                dataRecv!!.title+"notificatin..",
                dataRecv!!.body+"notificatin..",
                null)

        }
    }
}