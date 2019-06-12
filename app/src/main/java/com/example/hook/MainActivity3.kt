package com.example.hook

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.NotificationManagerCompat
import com.github.lzyzsd.jsbridge.BridgeHandler
import kotlinx.android.synthetic.main.activity_main.*
import com.github.lzyzsd.jsbridge.CallBackFunction
import org.jetbrains.anko.*


class MainActivity3 : AppCompatActivity(), AnkoLogger {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //val intent = Intent(this, MyNotifyService.class)
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

//        val intent = Intent(this@MainActivity, MyNotifyService::class.java)
//        startService(intent)

        startService<MyNotifyService>("pkg" to "")

        //bridge_webview.loadUrl("https://www.qq.com")
        bridge_webview.loadUrl("file:///android_asset/web.html")

        bridge_webview.setDefaultHandler(BridgeHandler() {
                data, function ->
            val msg = "默认接收到js的数据：" + data
            toast(msg)
            function.onCallBack("java默认接收完毕，并回传数据给js"); //回传数据给js
        })

        bridge_webview.registerHandler("submitFromWeb", BridgeHandler { data, callabck ->
            info( "submit data from web: $data")
            callabck.onCallBack("response data from Java")
        })


        button.setOnClickListener {
            bridge_webview.send("hello", CallBackFunction() { data -> toast(data) })
        }

        button2.setOnClickListener {
            //bridge_webview.callHandler("funcInJs", Gson().toJson(data), CallBackFunction { })
            bridge_webview.callHandler("functionInJs", "发送数据给js指定接收",
                CallBackFunction() { data -> toast(data) })

        }
    }

    fun isNotificationListenerEnabled(): Boolean {
        val packageNames = NotificationManagerCompat.getEnabledListenerPackages(this)
        return packageNames.contains(packageName)
    }
}
