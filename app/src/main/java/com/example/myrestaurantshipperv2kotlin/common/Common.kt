package com.example.myrestaurantshipperv2kotlin.common

import android.R.attr
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.myrestaurantshipperv2kotlin.R
import com.example.myrestaurantshipperv2kotlin.model.ShipperUserModel
import com.example.myrestaurantshipperv2kotlin.model.TokenModel
import com.example.myrestaurantshipperv2kotlin.services.MyFCMServices
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.FirebaseDatabase


object Common {
    fun setSpanStringColor(welcome: String, name: String, view: TextView, color: Int) {
        val builder = SpannableStringBuilder()
        builder.append(welcome)
        val spannableString = SpannableString(name)
        val boldSpan = StyleSpan(Typeface.BOLD)
        spannableString.setSpan(boldSpan, 0, name.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(
                ForegroundColorSpan(color),
                0,
                name.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        builder.append(spannableString)
        view.setText(builder, TextView.BufferType.SPANNABLE)
    }

    fun updateToken(context: Context, token: String, isServerToken: Boolean, isShipperToken: Boolean) {
        FirebaseDatabase.getInstance()
                .getReference(TOKEN_REF)
                .child(currentShipper!!.uid!!)
                .setValue(TokenModel(currentShipper!!.phone!!, token, isServerToken, isShipperToken))
                .addOnFailureListener { e ->
                    Toast.makeText(context, "" + e.message, Toast.LENGTH_SHORT).show()
                }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun showNotification(
            context: Context,
            id: Int,
            title: String?,
            content: String?,
            intent: Intent?
    ) {
        Log.d("Notification", "Tittle:$title, Content:$content")

        var pendingIntent: PendingIntent? = null
        if (intent != null) {
            pendingIntent =
                    PendingIntent.getActivity(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT)

            val NOTIFICATION_CHANNEL_ID = "pham.thuc.myrestaurantv2.server"

            val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val notificationChannel = NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    "My Restaurant V2",
                    NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationChannel.description = "My Restaurant V2 Channel"
            notificationChannel.enableLights(true)
            notificationChannel.enableVibration(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.vibrationPattern = longArrayOf(0, 1000, 500, 1000)

            notificationManager.createNotificationChannel(notificationChannel)

            val builder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            builder.setContentTitle(title).setContentText(content).setAutoCancel(true)
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setLargeIcon(
                            BitmapFactory.decodeResource(
                                    context.resources,
                                    R.drawable.ic_restaurant_24
                            )
                    )

            if (pendingIntent != null)
                builder.setContentIntent(pendingIntent)

            val notification = builder.build()
            notificationManager.notify(id, notification)
        }
        Log.d("Notification", "Ending notification")
    }

    fun getBearing(begin: LatLng, end: LatLng): Float {
        val lat = Math.abs(begin.latitude - end.longitude)
        val lng = Math.abs(begin.longitude - end.longitude)

        if (begin.latitude < end.latitude && begin.longitude < end.longitude)
            return Math.toDegrees(Math.atan(lng / lat)).toFloat()
        else if (begin.latitude >= end.latitude && begin.longitude < end.longitude)
            return (90 - Math.toDegrees(Math.atan(lng / lat)) + 90).toFloat()
        else if (begin.latitude >= end.latitude && begin.longitude >= end.longitude)
            return (Math.toDegrees(Math.atan(lng / lat)) + 180).toFloat()
        else if (begin.latitude < end.latitude && begin.longitude >= end.longitude)
            return (90 - Math.toDegrees(Math.atan(lng / lat)) + 270).toFloat()
        return -1.0f

    }

    fun decodePoly(encoded: String): List<LatLng> {
        val poly: MutableList<LatLng> = ArrayList<LatLng>()
        var index = 0
        var len = encoded.length
        var lat = 0
        var lng = 0
        while (index < len){
            var b: Int
            var shift=0
            var result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            }while (b >= 0x20)

            val dlat = if(result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat
            shift = 0
            result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            }while (b >= 0x20)

            val dlng = if(result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng
            val p = LatLng(lat.toDouble() / 1E5, lng.toDouble()/1E5)
            poly.add(p)
        }
        return poly
    }

    val TRIP_START: String? = "Trip"
    val SHIPPING_DATA: String = "ShippingData"
    val NOTI_CONTENT: String = "Content"
    val NOTI_TITLE: String = "Title"
    private val TOKEN_REF: String = "Tokens"
    val SHIPPING_ORDER_REF: String = "ShipperOrders"
    var currentShipper: ShipperUserModel? = null
    val SHIPPER_REF: String = "Shippers"
}