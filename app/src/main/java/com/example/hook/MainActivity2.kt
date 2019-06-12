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


class MainActivity2 : AppCompatActivity(), AnkoLogger {

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
        bridge_webview.loadUrl("file:///android_asset/web.html");

        //bridge_webview.setDefaultHandler(DefaultHandler())
        bridge_webview.registerHandler("submit", BridgeHandler { data, callabck ->
            info( "submit data from web: $data")
            callabck.onCallBack("response data from Java")
        })
        // web
//        WebViewJavascriptBridge.callHandler(
//            'submitFromWeb'
//            , {'param': str1}
//            , function(responseData) {
//                document.getElementById("show").innerHTML = "send get responseData from java, data = " + responseData
//            }
//        );

        button.setOnClickListener {
            bridge_webview.send("hello")
        }
        // web
//         bridge.init(function(message, responseCallback) {
//            console.log('JS got a message', message);
//            var data = {
//                'Javascript Responds': 'Wee!'
//            };
//            console.log('JS responding with', data);
//            responseCallback(data);
//        });

//        button2.setOnClickListener {
//            bridge_webview.callHandler("funcInJs", Gson().toJson(data), CallBackFunction { })
//        }
        // web
//        WebViewJavascriptBridge.registerHandler("functionInJs", function(data, responseCallback) {
//            document.getElementById("show").innerHTML = ("data from Java: = " + data);
//            var responseData = "Javascript Says Right back aka!";
//            responseCallback(responseData);
//        });

    }

    fun isNotificationListenerEnabled(): Boolean {
        val packageNames = NotificationManagerCompat.getEnabledListenerPackages(this)
        return packageNames.contains(packageName)
    }
}
