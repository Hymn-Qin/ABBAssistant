package com.foxconn.abbassistant

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.RemoteException
import de.greenrobot.event.EventBus


@SuppressLint("Registered")
open
/**
 * Created by xiao-jie.qin@mail.foxconn.com on 2018/5/26.
 */

class AssistantAudioService : Service() {
    private val assistantAudioInterface = object : AssistantAudioInterface.Stub() {
        override fun getByte(audioData: AudioData?) {
            EventBus.getDefault().post(AssistantMessageEvent("AUDIO", null, null, audioData!!.pcm, null))
        }

        @Throws(RemoteException::class)
        override fun getAudioMode(type: Int) {
            EventBus.getDefault().post(AssistantMessageEvent("SWITCH_MODE", type, null, null, null))
        }

        @Throws(RemoteException::class)
        override fun setMessage(type: Int, message: String?) {
            EventBus.getDefault().post(AssistantMessageEvent("DEVICE", type, message, null, null))
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return assistantAudioInterface
    }

}
