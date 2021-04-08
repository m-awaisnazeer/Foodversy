package com.communisolve.foodversy.common

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.communisolve.foodversy.R
import com.communisolve.foodversy.model.*
import com.google.firebase.database.FirebaseDatabase
import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.random.Random

object Common {
    fun formatPrice(price: Double): String {
        if (price != 0.toDouble()) {
            val df = DecimalFormat("#,##0.00")
            df.roundingMode = RoundingMode.HALF_UP
            val finalPrice = StringBuilder(df.format(price)).toString()
            return finalPrice.replace(".", ",")
        } else return "0,00"
    }

    fun calculateExtraPrice(
        userSelectedSize: SizeModel?,
        userSelectedAddon: MutableList<AddOnModel>?
    ): Double {
        var result: Double = 0.0
        if (userSelectedSize == null && userSelectedAddon == null) {
            return 0.0
        } else if (userSelectedSize == null) {
            for (addonModel in userSelectedAddon!!) {
                result += addonModel.price.toDouble()
            }
            return result
        } else if (userSelectedAddon == null) {
            result = userSelectedSize.price.toDouble()
            return result
        } else {
            result = userSelectedSize.price.toDouble()
            for (addonModel in userSelectedAddon!!)
                result += addonModel.price.toDouble()
            return result
        }
    }

    fun createOrderNumber(): String {
        return StringBuilder()
            .append(System.currentTimeMillis())
            .append(Math.abs(Random.nextInt()))
            .toString()
    }

    fun getDateOfWeek(i: Int): String {
        when(i){
            1-> return "Sunday"
            2-> return "Monday"
            3-> return "Tuesday"
            4-> return "Wednesday"
            5-> return "Thursday"
            6-> return "Friday"
            7-> return "Saturday"
            else -> return "Unk"
        }
    }

    fun convertStatusToText(orderStatus: Int): String {
        when(orderStatus){
            0-> return "Placed"
            1-> return "Shipping"
            2-> return "Shipped"
            -1 -> return "Cancelled"
            else -> return "Unk"
        }
    }

    fun buildToken(authorizeToken: String?): String {
        return StringBuilder("Bearer").append(" ").append(authorizeToken).toString()
    }

    fun updateToken(context: Context, token: String) {
        FirebaseDatabase.getInstance().getReference(Common.TOKEN_REF)
            .child(Common.currentUser!!.uid)
            .setValue(TokenModel(currentUser!!.uid,token))
            .addOnFailureListener { Toast.makeText(context, "$token", Toast.LENGTH_SHORT).show() }
            .addOnSuccessListener {  }

    }

    fun showNotification(
        context: Context, id: Int, title: String?, content: String?,
        intent: Intent?
    ) {
        var pendingInent:PendingIntent? = null
        if (intent !=null)
            pendingInent = PendingIntent.getActivity(context,id,intent,PendingIntent.FLAG_UPDATE_CURRENT)
        val NOTIFICATION_CHANNEL_ID = "com.communisolve.foodversy"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val notificationChannel = NotificationChannel(NOTIFICATION_CHANNEL_ID,"Foodversy",NotificationManager.IMPORTANCE_DEFAULT)

            notificationChannel.description = "Foodversy"
            notificationChannel.enableLights(true)
            notificationChannel.enableVibration(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.vibrationPattern = longArrayOf(0,1000,500,1000)

            notificationManager.createNotificationChannel(notificationChannel)
        }
        val builder  = NotificationCompat.Builder(context,NOTIFICATION_CHANNEL_ID)

        builder.setContentTitle(title).setContentText(content).setAutoCancel(true)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setLargeIcon(BitmapFactory.decodeResource(context.resources,R.drawable.ic_baseline_restaurant_menu_24))
        if (pendingInent != null)
            builder.setContentIntent(pendingInent)

        val notification = builder.build()
        notificationManager.notify(id,notification)
    }

    val NOTI_CONTENT: String?="content"
    val NOTI_TITLE: String?="title"
    private val TOKEN_REF: String="Tokens"
    var authorizeToken: String?=null
    var currentToken: String=""
    val ORDER_REF: String="Orders"
    val COMMENT_REF: String = "Comments"
     var foodSelected: FoodModel?=null
     var categorySelected: CategoryModel?=null
    val CATEGORY_REF: String = "Category"
    val FULL_WIDTH_COLUMN: Int = 1
    val DEFAULT_COLUMN_COUNT: Int = 0
    val BEST_DEALS_REF: String = "BestDeals"
    val POPULAR_REF: String = "MostPopular"
    val USER_REFERENCE = "Users"
    var currentUser: UserModel? = null

    var userSelectedAddon: List<AddOnModel>? = null
    var userSelectedSize: SizeModel? = null
}