package com.example.hook

import android.annotation.TargetApi
import android.app.*
import android.content.*
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.NotificationManagerCompat
import com.github.lzyzsd.jsbridge.BridgeHandler
import kotlinx.android.synthetic.main.activity_main.*
import com.github.lzyzsd.jsbridge.CallBackFunction
import org.jetbrains.anko.*
import android.net.Uri
import android.os.Build
import android.util.Log
import com.example.hook.jsbridgewebview.WebviewGlobals
import com.example.hook.myjswebview.MyjsWebview
import android.content.pm.PackageManager
import android.content.ComponentName
import android.support.v4.app.NotificationCompat
import android.view.KeyEvent
import android.widget.Toast
import android.view.KeyEvent.KEYCODE_BACK
import kotlinx.android.synthetic.main.activity_main4.*
import android.content.Intent
import android.media.RingtoneManager
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.widget.Adapter
import kotlinx.android.synthetic.main.activity_main4.view.*


class MainActivity4 : AppCompatActivity(), AnkoLogger {

    companion object {
        val ACTION_NOTIFY = "com.action.notify"
        val TAG = MainActivity4.javaClass.simpleName
    }
    var notificationId = "channel_001"
    var notificationName = "name"

    val list: ArrayList<Movie> = arrayListOf()
    val adapter = WeAdapter(list)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main4)

        enableNotify()
        // anko
        //startService<MyNotifyService>("pkg" to "com.eg.android.AlipayGphone")
        startService<MyNotifyService>("pkg" to "com.tencent.mm")

        initBroadcast()

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        list.add(Movie("test", "sky"))
        adapter.notifyDataSetChanged()
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        notify.setOnClickListener {
//            val id = "channel_001"
//            val name = "name"
//            val notificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//
//            var notification: Notification? = null
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {//判断API
//                val mChannel = NotificationChannel(id, name, NotificationManager.IMPORTANCE_LOW)
//                notificationManager.createNotificationChannel(mChannel)
//                notification = Notification.Builder(this)
//                    .setChannelId(id)
//                    .setContentTitle("活动")
//                    .setContentText("您有一项新活动")
//                    .setSmallIcon(R.mipmap.ic_launcher_round).build()
//            } else {
//                val notificationBuilder = NotificationCompat.Builder(this)
//                    .setContentTitle("活动")
//                    .setContentText("您有一项新活动")
//                    .setSmallIcon(R.mipmap.ic_launcher_round)
//                    .setOngoing(true)
//                    .setChannelId(id)//无效
//                notification = notificationBuilder.build()
//            }
//
//            notificationManager.notify(1, notification)
            sendNotify("测试", "hello")
        }

        clear.setOnClickListener {
            list.clear()
            adapter.notifyDataSetChanged()
        }
    }

    private fun sendNotify(title: String, content: String) {
        info("sendNotify: " + title + content)
        val intent = Intent(this, MainActivity4::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
        val notificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        var notification: Notification? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {//判断API
            val mChannel = NotificationChannel(notificationId, notificationName, NotificationManager.IMPORTANCE_LOW)
            notificationManager.createNotificationChannel(mChannel)
            notification = Notification.Builder(this, notificationId)
                .setContentTitle(title)
                .setContentText(content)
                .setContentIntent(pendingIntent)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setVibrate(longArrayOf(500, 500, 1000, 2000, 3000, 500, 500, 500))
                .setSmallIcon(R.mipmap.ic_launcher_round).build()
        } else {
            val notificationBuilder = NotificationCompat.Builder(this)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
            notification = notificationBuilder.build()
        }

        notificationManager.notify(1, notification)
        list.add(Movie(title, content))
        adapter.notifyDataSetChanged()
    }

    override fun onStart() {
        super.onStart()

        toggleNotificationListenerService()
    }

    private fun toggleNotificationListenerService() {
        info("toggleNotificationListenerService")
        val pm = packageManager
        pm.setComponentEnabledSetting(
            ComponentName(
                this,
                com.example.hook.MyNotifyService::class.java!!
            ),
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP
        )

        pm.setComponentEnabledSetting(
            ComponentName(
                this,
                com.example.hook.MyNotifyService::class.java!!
            ),
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP
        )

    }


    fun enableNotify() {
        if (isNotificationListenerEnabled()) {
            toast("通知权限已经被打开" +
                    "\n手机型号:${android.os.Build.MODEL}" +
                    "\nSDK版本:${android.os.Build.VERSION.SDK_INT}" +
                    "\n系统版本:${android.os.Build.VERSION.RELEASE}" +
                    "\n软件包名:${getPackageName()}")
        } else {
            alert("通知权限没有被开启，点击去开启", "开启通知") {
                yesButton { startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) }
                noButton {  }
            }.show()

        }
    }

    fun isNotificationListenerEnabled(): Boolean {
        val packageNames = NotificationManagerCompat.getEnabledListenerPackages(this)
        return packageNames.contains(packageName)
    }


    fun initBroadcast() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(ACTION_NOTIFY)
        registerReceiver(mBroadcastReceiver, intentFilter)
    }



    var firstTime: Long = 0
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        val secondTime = System.currentTimeMillis()

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (secondTime - firstTime < 2000) {
                System.exit(0)
            } else {
                Toast.makeText(applicationContext, "再按一次返回键退出", Toast.LENGTH_SHORT).show()
                firstTime = System.currentTimeMillis()
            }

            return true
        }

        return super.onKeyDown(keyCode, event)
    }


    val mBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

            val title = intent?.extras?.getString("title") ?: "no title"
            val content = intent?.extras?.getString("content") ?: "no content"
            info( "recv msg" + content)
            toast(title + ": " + content)

            sendNotify(title, content)
        }
    }

}
