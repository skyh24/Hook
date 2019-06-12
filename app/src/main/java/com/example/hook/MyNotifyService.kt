package com.example.hook

import android.content.Intent
import android.os.*
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.support.v4.app.NotificationCompat
import android.widget.Toast
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info

class MyNotifyService : NotificationListenerService(), AnkoLogger {

//    inner class MyBinder : Binder() {
//        val service: MyNotifyService
//            get() = this@MyNotifyService
//    }
//
//    val mBinder = MyBinder()
    var pkg: String = ""

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        info("onStartCommand")
        pkg = intent?.getStringExtra("pkg") ?: ""
        return START_REDELIVER_INTENT
    }

    // 不可以onBindService
//    override fun onBind(intent: Intent?): IBinder? {
//        pkg = intent?.getStringExtra("pkg") ?: ""
//        info("onBind:" + pkg)
//        return mBinder
//    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        info("onNotificationPosted")
        if (pkg == "" || pkg == sbn?.packageName) {
            val msg = sbn?.notification?.tickerText ?: ""
            val title = sbn?.notification?.extras?.getString(NotificationCompat.EXTRA_TITLE) ?: ""
            val content = sbn?.notification?.extras?.getString(NotificationCompat.EXTRA_TEXT) ?: ""

            info { sbn?.packageName + " " + msg + " title:" + title + " text:" + content }
            //Toast.makeText(applicationContext, msg, Toast.LENGTH_LONG).show()

            val intent = Intent(MainActivity.ACTION_NOTIFY)
            intent.putExtra("msg", msg)
            intent.putExtra("title", title)
            intent.putExtra("content", content)
            sendBroadcast(intent)

        }

        super.onNotificationPosted(sbn)
    }




}