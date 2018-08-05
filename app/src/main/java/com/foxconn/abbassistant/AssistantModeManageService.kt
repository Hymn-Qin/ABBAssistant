package com.foxconn.abbassistant

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * Created by xiao-jie.qin@mail.foxconn.com on 2018/5/30.
 */
class AssistantModeManageService : Service(){
    override fun onBind(intent: Intent?): IBinder {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCreate() {
        startForeground(1, Notification())
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        initData()
        return super.onStartCommand(intent, flags, startId)
    }

    private fun initData() {

    }
}