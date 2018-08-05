package com.foxconn.abbassistant

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle

class AssistantActivity : Activity(){
    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)
        val serIntent = Intent(this, AssistantService::class.java)
        serIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        this.startForegroundService(serIntent)
    }
}
