package com.example.hook

import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import com.squareup.okhttp.Request
import com.zhy.http.okhttp.OkHttpUtils
import com.zhy.http.okhttp.callback.StringCallback
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.json.JSONObject
import java.lang.Exception

class Show: AppCompatActivity(), AnkoLogger {

    var token: String? = null
    val msgUrl = "https://www.cubedex.io/otc/auto_callback_reg"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        token = getLoginToken()
        if (token == null) {
            finish()
            return
        }
        info("token: " + token)
        MainUI2().setContentView(this@Show)
        //startService<MyNotifyService>("pkg" to "com.tencent.mm")
        startService<MyNotifyService>("pkg" to "com.eg.android.AlipayGphone")
        initBroadcast()
    }

    override fun onStart() {
        super.onStart()
        toggleNotificationListenerService()
    }

    override fun onDestroy() {
        info("Show onDestroy")
        stopService<MyNotifyService>()
        unregisterReceiver(mBroadcastReceiver)
        super.onDestroy()
    }

    fun getLoginToken(): String? {
        val pref = getSharedPreferences("data", Context.MODE_PRIVATE)
        return pref.getString("token", null)
    }

    fun clearToken() {
        val edit = getSharedPreferences("data", Context.MODE_PRIVATE).edit()
        edit.clear()
        edit.commit()
    }

    fun initBroadcast() {
        info("Show initBroadcast")
        val intentFilter = IntentFilter()
        intentFilter.addAction(MainActivity4.ACTION_NOTIFY)
        registerReceiver(mBroadcastReceiver, intentFilter)
    }

    private fun toggleNotificationListenerService() {
        info("Show toggleNotificationListenerService")
        val pm = packageManager
        pm.setComponentEnabledSetting(
            ComponentName(
                this,
                MyNotifyService::class.java!!
            ),
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP
        )

        pm.setComponentEnabledSetting(
            ComponentName(
                this,
                MyNotifyService::class.java!!
            ),
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP
        )

    }

    val mBroadcastReceiver =  MyBroadcastReceiver()
    val list: ArrayList<Movie> = arrayListOf()
    val mAdapter = MyAdapter(list)

    inner class MyBroadcastReceiver: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val title = intent?.extras?.getString("title") ?: "no title"
            val content = intent?.extras?.getString("content") ?: "no content"
            //list.add(Movie(title, content))
            //mAdapter.notifyDataSetChanged()
            info( "BroadcastReceiver: " + content)

            OkHttpUtils.post().url(msgUrl)
                .addHeader("Authorization", token)
                .addParams("content", content ).build()
                .execute(object : StringCallback() {
                    override fun onResponse(response: String?) {
                        val json = JSONObject(response)
                        val code = json.getInt("code")
                        info("onResponse " + code + ":"+ response)
                        list.add(Movie(content, response))
                        mAdapter.notifyDataSetChanged()
                        if (code == -1) {
                            toast("Response:"+ response)
                            stopService<MyNotifyService>()
                            clearToken()
                            finish()
                        }
                    }

                    override fun onError(request: Request?, e: Exception?) {
                        toast(content + "\nServer err!!!\n" + e.toString())
                        list.add(Movie(content, e.toString()))
                        mAdapter.notifyDataSetChanged()
                        error("onError" + e.toString())
                    }

                })

        }
    }

    inner class MainUI2 : AnkoComponent<Show>, AnkoLogger {
        override fun createView(ui: AnkoContext<Show>) = with(ui) {
            verticalLayout {
                lparams(matchParent, matchParent)
                button("退出登录") {
                    backgroundColor = Color.parseColor("#75E1FF")
                    alpha = 0.5f
                    onClick {
                        stopService<MyNotifyService>()
                        clearToken()
                        finish()
                    }
                }.lparams(matchParent, 200) {
                    horizontalMargin = dip(10)
                    topMargin = dip(10)
                }

                listView {
                    adapter = mAdapter
                }
            }
        }


    }
}

