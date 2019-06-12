package com.example.hook.myjswebview

import android.content.Context
import com.github.lzyzsd.jsbridge.BridgeWebView

class MyjsWebview : BridgeWebView {
    companion object {
        val TAG = MyjsWebview.javaClass.simpleName
        val ENCODE = "utf-8"
        val SCALE = 100
    }

    lateinit var mContext: Context

    constructor(context: Context) : super(context) {
        mContext
    }


}