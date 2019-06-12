package com.example.hook

import android.annotation.TargetApi
import android.app.Activity
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







class MainActivity : AppCompatActivity(), AnkoLogger {

    companion object {
        val ACTION_NOTIFY = "com.action.notify"
        val TAG = MyjsWebview.javaClass.simpleName
    }

//    var isBound = false
//    var mService: MyNotifyService? = null
//    private val conn = object : ServiceConnection {
//        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
//            isBound = true
//            val mBinder = binder as MyNotifyService.MyBinder
//            mService = mBinder.service
//            Log.i(TAG, "Service Connected")
//        }
//
//        override fun onServiceDisconnected(name: ComponentName) {
//            isBound = false
//            Log.i(TAG, "Service Disconnected")
//        }
//    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        enableNotify()
        // anko
        //startService<MyNotifyService>("pkg" to "com.eg.android.AlipayGphone")
        startService<MyNotifyService>("pkg" to "com.tencent.mm")
        // 用了bind方式

        //initService()
        initBroadcast()
        initBridge()
        initListener()
    }
//
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

    override fun onDestroy() {
        super.onDestroy()

//        if(isBound) {
//            unbindService(conn)
//            isBound = false
//        }
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

    fun initService() {
        val intent = Intent(this@MainActivity, MyNotifyService::class.java)
        intent.putExtra("pkg", "com.tencent.mm")
        //intent.putExtra("pkg", "com.eg.android.AlipayGphone")
        //bindService(intent, conn, Context.BIND_AUTO_CREATE)
        startService(intent)
    }

    fun initBroadcast() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(ACTION_NOTIFY)
        registerReceiver(mBroadcastReceiver, intentFilter)
    }

    fun initBridge() {
        //bridge_webview.loadUrl("http://www.listenmi.cn")
        //bridge_webview.loadUrl("http://192.168.0.104:8080")
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
    }

    fun initListener() {
        button.setOnClickListener {
            toast("hello")
            bridge_webview.send("hello", CallBackFunction() { data -> toast(data) })
        }

        button2.setOnClickListener {
            //bridge_webview.callHandler("funcInJs", Gson().toJson(data), CallBackFunction { })
            bridge_webview.callHandler("functionInJs", "发送数据给js指定接收",
                CallBackFunction() { data -> toast(data) })

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.w(TAG, "{onActivityResult}resultCode=$resultCode")
        Log.w(TAG, "{onActivityResult}requestCode=$requestCode")
        Log.w(TAG, "{onActivityResult}data=$data")
        if (resultCode == Activity.RESULT_OK) {
            //webview界面调用打开本地文件管理器选择文件的回调
            if (requestCode == WebviewGlobals.CHOOSE_FILE_REQUEST_CODE) {
                val result = data?.data
                Log.w(TAG, "{onActivityResult}文件路径地址：" + result!!.toString())

                //如果mUploadMessage或者mUploadCallbackAboveL不为空，代表是触发input[type]类型的标签
                if (null != bridge_webview.getMyBridgeWebChromeClient().getmUploadMessage() || null != bridge_webview.getMyBridgeWebChromeClient().getmUploadCallbackAboveL()) {
                    if (bridge_webview.getMyBridgeWebChromeClient().getmUploadCallbackAboveL() != null) {
                        onActivityResultAboveL(requestCode, data)//5.0++
                    } else if (bridge_webview.getMyBridgeWebChromeClient().getmUploadMessage() != null) {
                        bridge_webview.getMyBridgeWebChromeClient().getmUploadMessage()
                            .onReceiveValue(result)//将文件路径返回去，填充到input中
                        bridge_webview.getMyBridgeWebChromeClient().setmUploadMessage(null)
                    }
                } else {
                    //此处代码是处理通过js方法触发的情况
                    Log.w(TAG, "{onActivityResult}文件路径地址(js)：$result")
                }
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {//resultCode == RESULT_CANCELED 解决不选择文件，直接返回后无法再次点击的问题
            if (bridge_webview.getMyBridgeWebChromeClient().getmUploadMessage() != null) {
                bridge_webview.getMyBridgeWebChromeClient().getmUploadMessage().onReceiveValue(null)
                bridge_webview.getMyBridgeWebChromeClient().setmUploadMessage(null)
            }
            if (bridge_webview.getMyBridgeWebChromeClient().getmUploadCallbackAboveL() != null) {
                bridge_webview.getMyBridgeWebChromeClient().getmUploadCallbackAboveL().onReceiveValue(null)
                bridge_webview.getMyBridgeWebChromeClient().setmUploadCallbackAboveL(null)
            }
        }
    }

    //5.0以上版本，由于api不一样，要单独处理
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun onActivityResultAboveL(requestCode: Int, data: Intent?) {

        if (bridge_webview.getMyBridgeWebChromeClient().getmUploadCallbackAboveL() == null) {
            return
        }
        var result: Uri? = null
        if (requestCode == WebviewGlobals.CHOOSE_FILE_REQUEST_CODE) {//打开本地文件管理器选择图片
            result = data?.data
        }
        Log.w(TAG, "{onActivityResultAboveL}文件路径地址：" + result!!.toString())
        bridge_webview.getMyBridgeWebChromeClient().getmUploadCallbackAboveL()
            .onReceiveValue(arrayOf(result))//将文件路径返回去，填充到input中
        bridge_webview.getMyBridgeWebChromeClient().setmUploadCallbackAboveL(null)
        return
    }

    val mBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

            val title = intent?.extras?.getString("title") ?: "no title"
            val content = intent?.extras?.getString("content") ?: "no content"
            info( "recv msg" + content)
            toast(title + ": " + content)
            bridge_webview.send(content, CallBackFunction() { data -> toast(data) })
        }
    }


    fun isNotificationListenerEnabled(): Boolean {
        val packageNames = NotificationManagerCompat.getEnabledListenerPackages(this)
        return packageNames.contains(packageName)
    }
}
