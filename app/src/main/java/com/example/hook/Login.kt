package com.example.hook


import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.NotificationManagerCompat
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.view.KeyEvent
import android.widget.Toast
import com.squareup.okhttp.Request
import com.zhy.http.okhttp.OkHttpUtils
import com.zhy.http.okhttp.callback.StringCallback

import org.jetbrains.anko.*
import org.jetbrains.anko.button
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.json.JSONObject
import java.lang.Exception


class Login: AppCompatActivity() {
    companion object {
        val LoginUrl = "https://www.cubedex.io/users/sign_in"
        val Version = "v1.03"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableNotify()
        // check pref
        val pref = getSharedPreferences("data", Context.MODE_PRIVATE)
        if (pref.getString("token", null) != null) {
            startActivity<Show>()
            finish()
            return
        }
        // check front
        if (intent.flags and Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT != 0) {
            //结束你的activity
            finish()
            return
        }
        super.onCreate(savedInstanceState)
        MainUI().setContentView(this@Login)

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

    fun isNotificationListenerEnabled(): Boolean {
        val packageNames = NotificationManagerCompat.getEnabledListenerPackages(this)
        return packageNames.contains(packageName)
    }

    fun enableNotify() {
        if (isNotificationListenerEnabled()) {
            toast("通知权限已经被打开" + "柚子助手: 1.0")
        } else {
            alert("通知权限没有被开启，点击去开启", "开启通知") {
                yesButton { startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) }
                noButton {  }
            }.show()

        }
    }

    fun setLoginToken(token: String) {
        val edit = getSharedPreferences("data", Context.MODE_PRIVATE).edit()
        edit.putString("token", token)
        edit.commit()
    }

    fun SHA1(decript: String): String {
        try {
            val digest = java.security.MessageDigest
                .getInstance("SHA-1")
            digest.update(decript.toByteArray())
            val messageDigest = digest.digest()
            // Create Hex String
            val hexString = StringBuffer()
            // 字节数组转换为 十六进制 数
            for (i in messageDigest.indices) {
                val shaHex = Integer.toHexString(messageDigest[i].toInt() and 0xFF)
                if (shaHex.length < 2) {
                    hexString.append(0)
                }
                hexString.append(shaHex)
            }
            return hexString.toString()

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return ""
    }

    inner class MainUI : AnkoComponent<Login>, AnkoLogger {
        override fun createView(ui: AnkoContext<Login>) = with(ui) {
            verticalLayout {
                gravity = Gravity.CENTER_HORIZONTAL
                lparams(matchParent, matchParent)
//            textView { text = SHA1("bys" +SHA1("123")) }
                val edPhone = editText {
                    gravity = Gravity.CENTER_VERTICAL
                    hint = "phone"
                }.lparams(dip(250), 180)
                val edPass = editText {
                    gravity = Gravity.CENTER_VERTICAL
                    hint = "password"
                }.lparams(dip(250), 180)

                button("登录") {
                    backgroundColor = Color.parseColor("#75E1FF")
                    alpha = 0.5f
                    onClick {
                        var phone = edPhone.text.toString()
                        var pass = edPass.text.toString()
                        pass = SHA1("bys" + SHA1(pass))
                        info( phone +" "+ pass)
                        OkHttpUtils.post().url(Login.LoginUrl)
                            .addParams("phone", phone )
                            .addParams("pwd", pass).build()
                            .execute(object : StringCallback() {
                                override fun onResponse(response: String?) {
                                    val json = JSONObject(response)
                                    val code = json.getInt("code")
                                    info("onResponse " + code + ":"+ response)
                                    if(code == 0) {
                                        val data = json.getJSONObject("data")
                                        val token = data.getString("token")
                                        setLoginToken(token)
                                        startActivity<Show>()
                                    } else {
                                        toast(response.toString())
                                        error("onResponse " + code + ":" + response)
                                    }
                                }

                                override fun onError(request: Request?, e: Exception?) {
                                    toast("Server err!!!\n" + e.toString())
                                    error("onError" + e.toString())
                                }

                            })
                    }
                }.lparams(dip(250), 150) {topMargin = dip(10)}
                textView("西柚钱包: " + Login.Version) {
                    gravity = Gravity.CENTER
                }
            }
        }
    }
}

