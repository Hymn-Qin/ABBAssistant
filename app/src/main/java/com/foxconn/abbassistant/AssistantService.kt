package com.foxconn.abbassistant

import android.annotation.SuppressLint
import android.app.Notification
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.AudioManager
import android.os.Handler
import android.os.IBinder
import android.util.Log
import com.aispeech.dui.dds.DDS
import com.aispeech.dui.dds.DDSAuthListener
import com.aispeech.dui.dds.DDSConfig
import com.aispeech.dui.dds.DDSInitListener
import com.aispeech.dui.dds.agent.MessageObserver
import com.aispeech.dui.dds.agent.TTSEngine
import com.aispeech.dui.dds.auth.AuthType
import com.aispeech.dui.dds.exceptions.DDSNotInitCompleteException
import com.aispeech.dui.dds.update.DDSUpdateListener
import com.aispeech.dui.dsk.duiwidget.CommandObserver
import com.foxconn.abbassistant.AssistantData.ALIAS_KEY
import com.foxconn.abbassistant.AssistantData.API_KEY
import com.foxconn.abbassistant.AssistantData.API_KEYS
import com.foxconn.abbassistant.AssistantData.COMMAND_MESSAGE
import com.foxconn.abbassistant.AssistantData.COMMAND_VOICE
import com.foxconn.abbassistant.AssistantData.PRODUCT_ID
import com.foxconn.abbassistant.AssistantData.PRODUCT_IDS
import com.foxconn.abbassistant.AssistantData.TYPE_MODE_AUDIO
import com.foxconn.abbassistant.AssistantData.USER_ID
import com.foxconn.abbassistant.AssistantData.UUID_PATH
import com.foxconn.abbassistant.AssistantData.assistantStatus
import com.foxconn.abbassistant.AssistantData.deviceStatus
import com.foxconn.abbassistant.AssistantData.isAuthing
import com.foxconn.abbassistant.AssistantData.isDoAuth
import com.foxconn.abbassistant.AssistantData.isInitOk
import com.foxconn.abbassistant.AssistantData.isIniting
import com.foxconn.abbassistant.AssistantData.isNoises
import com.foxconn.abbassistant.AssistantData.isOK
import com.foxconn.abbassistant.AssistantData.isRunning
import com.foxconn.abbassistant.AssistantData.isTTSing
import com.foxconn.abbassistant.AssistantData.isUpdated
import com.foxconn.abbassistant.AudioNoise.getID
import com.foxconn.abbmanagerservice.AssistantManager
import de.greenrobot.event.EventBus
import de.greenrobot.event.Subscribe
import de.greenrobot.event.ThreadMode
import org.json.JSONObject

class AssistantService : Service() {

    private val TAG = "AssistantService"
    private var assistantManager: AssistantManager? = null


    private val commandObserver = CommandObserver { command, data ->
        Log.d(TAG, "this command is $command  data is $data")
        when (command) {
            "open_window" -> {
//                val jsonData = JSONObject(data)
//                val intentName = jsonData.optString("intentName")
//                val w = jsonData.optString("w")
                //  依据w的值，执行打开窗户操作
            }
            "home.control" -> {//注册 在 AssistantData.COMMAND_VOICE
                //  处理调大音量的快捷唤醒指令
                val jsonData = JSONObject(data)
                val type = jsonData.optString("type")
                val name = jsonData.optString("name")
                val value = jsonData.optString("value")
                val values = jsonData.optString("values")
                homeControl(type, name, value, values)
            }
            "sys.upload.result" -> {

            }
        }
    }
    private val messageObserver = MessageObserver { message, data ->
        Log.d(TAG, "this message is $message  data is $data")
        when (message) {
            "sys.resource.updated" -> {
                updaterDds()
            }
            "sys.wakeup.result" -> {
                onBindService("")
                assistantManager?.basicTypes(1, 1, "语音")
            }
            "sys.dialog.start" -> {
                onBindService("")
                assistantManager?.basicTypes(1, 1, "语音")
            }
            "sys.dialog.end" -> {
                onBindService("")
                assistantManager?.basicTypes(2, 1, "语音")
            }
        }
    }

    private fun homeControl(type: String?, name: String?, value: String?, values: String?) {
        Log.d(TAG, "homeControl , type : $type name : $name value : $value values : $values")
        onBindService(type)
        if (type == "电视") {
            when (name) {
                "音量" -> if (value == "+") assistantManager?.basicTypes(2, 50, "红外")
                else if (value == "-") assistantManager?.basicTypes(2, 51, "红外")//音量调节 name=音量  value= +或-  11  12
                "打开" -> assistantManager?.basicTypes(2, 1, "红外")//打开电视 name=打开
                "关闭" -> assistantManager?.basicTypes(2, 1, "红外")//关闭电视 name=关闭
//                "切换频道" -> when (value) {
//                    "1" -> assistantManager?.basicTypes(1, 1, null)
//                }//切换频道 name=切换频道  value= 1 2 4  values = 江苏卫视
                "频道加" -> assistantManager?.basicTypes(2, 43, "红外")//频道加 name=频道加  fid 43
                "频道减" -> assistantManager?.basicTypes(2, 44, "红外")//频道减 name=频道减  fid 44
//                "频道返回" -> assistantManager?.basicTypes(1, 1, null)//频道返回 name=频道返回
            }
        }
    }

    private fun onBindService(type: String?) {
        if (!onAidlStatus) {
            attemptToBindService(type)
            return
        }
    }

    private var onAidlStatus: Boolean = false
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            onAidlStatus = false
        }


        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d(TAG, "onServiceConnected")
            onAidlStatus = true
            assistantManager = AssistantManager.Stub.asInterface(service)
            homeControl(type, null, null, null)
        }
    }


    override fun onBind(intent: Intent): IBinder? {
        // TODO: Return the communication channel to the service.
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onCreate() {
        startForeground(1, Notification())
        if (!EventBus.getDefault().hasSubscriberForEvent(AssistantMessageEvent::class.java)) {
            EventBus.getDefault().register(this)
        }
        Log.d(TAG, "OnCreate assistant")
        super.onCreate()
    }

    private var type: String? = null
    private fun attemptToBindService(type: String?) {
        this.type = type
        val intent = Intent()
        intent.`package` = "com.foxconn.abbmanagerservice"
        intent.action = "com.foxconn.assistantcommand"
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    @SuppressLint("WrongConstant")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "OnStartCommand assistant")
        USER_ID = getID(UUID_PATH)!!
        isRunning = true
        attemptToBindService(type)
        init()
        val flag = Service.START_STICKY
        return super.onStartCommand(intent, flag, startId)
    }


    private fun init() {
        isIniting = true
        DDS.getInstance().setDebugMode(2)//在调试时可以打开sdk调试日志，在发布版本时，请关闭
        DDS.getInstance().init(applicationContext, createConfig(), object : DDSInitListener {
            override fun onInitComplete(isFull: Boolean) {
                Log.d(TAG, "onInitComplete $isFull")
                isInitOk = isFull
                isIniting = false
                if (isFull) {
                    try {
                        DDS.getInstance().agent.subscribe(COMMAND_VOICE, commandObserver)
                        DDS.getInstance().agent.subscribe(COMMAND_MESSAGE, messageObserver)
                        // 打断
//                        DDS.getInstance().getAgent().getWakeupEngine().updateShortcutWakeupWord(WAKEUP_VOICE, WAKEUP_VOICE_PIN, WAKEUP_VOICE_PIN2)
                        //授权
                        DDS.getInstance().doAuth()
                        isAuthing = true
                        //调用后才能唤醒
                        if (assistantStatus)DDS.getInstance().agent.wakeupEngine.enableWakeup()
                        Log.d(TAG, "onAuthSuccess -->" + DDS.getInstance().isAuthSuccess)

                        TTSListener()
                        when (deviceStatus) {
                            "小安连接网络了" -> toTTS("小安连接网络了")
                            "热点打开了，快来帮小安设置网络吧" -> toTTS("热点打开了，快来帮小安设置网络吧")
                            "小安断开网络了" -> toTTS("小安断开网络了")
                            "小安更新完毕了" -> toTTS("小安更新完毕了")
                        }
                        deviceStatus = ""
                    } catch (e: DDSNotInitCompleteException) {
                        e.printStackTrace()
                    }
                } else {
                    EventBus.getDefault().post(AssistantMessageEvent("CHECK_INIT", null, null, null, null))
                }
            }

            override fun onError(what: Int, msg: String?) {
                Log.e(TAG, "Init onError: $what, error: $msg")
                isIniting = false
//                EventBus.getDefault().post(AssistantMessageEvent("CHECK_INIT", null, null, null, null))

            }

        }, object : DDSAuthListener {
            override fun onAuthSuccess() {
                isAuthing = false
                Log.d(TAG, "onAuthSuccess")
                isDoAuth = DDS.getInstance().isAuthSuccess
                if (isInitOk && isDoAuth) {
                    when (TYPE_MODE_AUDIO) {
                        0 -> toLog("现在是内置语音模式")
                        1 -> toLog("现在是外置语音模式")
                    }
                    updaterDds()
                }
//                if (isUpdated) {
//                    toTTS("小安已经更新好了，你现在可以跟我对话了，主人")
//                }
            }

            override fun onAuthFailed(errId: String?, error: String?) {
                Log.e(TAG, "onAuthFailed: $errId, error:$error")
                isAuthing = false
                EventBus.getDefault().post(AssistantMessageEvent("CHECK_AUTH", null, null, null, null))
            }
        })

    }

    private fun updaterDds() {
        try {
            DDS.getInstance().updater.update(object : DDSUpdateListener {
                override fun onUpdateFound(p0: String?) {
                    Log.d(TAG, "onUpdateFound detail $p0")
                    toLog("小安检测到有更新")
                    isUpdated = false
                    toTTS("小安正在更新中，等一下好吗")
                }

                override fun onUpgrade(p0: String?) {
                    Log.d(TAG, "onUpgrade to version $p0")
                }

                override fun onUpdateFinish() {
                    Log.d(TAG, "onUpdateFinish")
                    deviceStatus = "小安更新完毕了"
                    toLog("小安更新完毕了")
                    isUpdated = true
//                    toTTS("小安更新完毕了")
//                    DDS.getInstance().doAuth()
                    DDS.getInstance().release()
                    init()

                }

                override fun onError(p0: Int, p1: String?) {
                    Log.e(TAG, "onError what : $p0  error : $p1")
                }

                override fun onDownloadProgress(p0: Float) {
                    Log.d(TAG, "onDownloadProgress $p0")
                }

            })
        } catch (e: DDSNotInitCompleteException) {
            e.printStackTrace()
        }

    }

    private fun TTSListener() {
        DDS.getInstance().agent.ttsEngine.setListener(object : TTSEngine.Callback {
            override fun beginning(p0: String?) {
                isTTSing = true
                toLog("正在播报语音 $p0")
            }

            override fun end(p0: String?, p1: Int) {
                Thread.sleep(2000)
                isTTSing = false
                toLog("语音播报结束 $p0  ${if (p1 == 0) "正常结束" else "被打断结束"}")
            }

            override fun error(p0: String?) {
                Thread.sleep(2000)
                isTTSing = false
                toLog("语音播报错误 $p0")
            }

            override fun received(p0: ByteArray?) {
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        DDS.getInstance().agent.unSubscribe(commandObserver)
        DDS.getInstance().agent.unSubscribe(messageObserver)
        DDS.getInstance().release()
        EventBus.getDefault().unregister(this)//反注册EventBus

        stopForeground(true)
        val intent = Intent("android.intent.action.REBOOT_ASS_SERVER")
        sendBroadcast(intent)
    }

    private fun createConfig(): DDSConfig {

        val config = DDSConfig()

        config.addConfig(DDSConfig.K_PRODUCT_ID, PRODUCT_ID)  // TODO 填写自己的产品ID
        config.addConfig(DDSConfig.K_USER_ID, USER_ID)  // TODO 填写真是的用户ID
        config.addConfig(DDSConfig.K_ALIAS_KEY, ALIAS_KEY)   // TODO 填写产品的发布分支
        config.addConfig(DDSConfig.K_AUTH_TYPE, AuthType.PROFILE)
        config.addConfig(DDSConfig.K_API_KEY, API_KEY)  // TODO 填写API KEY


        config.addConfig(DDSConfig.K_VAD_TIMEOUT, "5000")
//        config.addConfig(DDSConfig.K_ONESHOT_MIDTIME, "1")
//        config.addConfig(DDSConfig.K_ONESHOT_ENDTIME, "1")
        config.addConfig(DDSConfig.K_USE_UPDATE_NOTIFICATION, "false")
        config.addConfig(DDSConfig.K_DUICORE_ZIP, "assistant.zip")
        config.addConfig(DDSConfig.K_CUSTOM_ZIP, "product.zip")
//        config.addConfig(DDSConfig.K_DUICORE_ZIP, Environment.getExternalStorageDirectory().getPath() + "/assistant/assets/assistant.zip")//Environment.getExternalStorageDirectory().getPath() + "/assistant/assets/
//        config.addConfig(DDSConfig.K_CUSTOM_ZIP, Environment.getExternalStorageDirectory().getPath() + "/assistant/assets/product.zip")
        config.addConfig(DDSConfig.K_CACHE_PATH, "")


        when (TYPE_MODE_AUDIO) {
            0 -> config.addConfig(DDSConfig.K_RECORDER_MODE, "internal")
            1 -> config.addConfig(DDSConfig.K_RECORDER_MODE, "external")
        }
        Log.i(TAG, "config -> " + config.toString())
        return config
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    fun onEventMainThread(event: AssistantMessageEvent) {
        when (event.getMessage()) {
            "SWITCH_MODE" -> {//camera确认语音模式
                toLog("是否打开（5）或关闭（4）语音助手" + event.getType())
                when (event.getType()) {
                    4 -> {
                        isOK = false
                    }
                    5 -> isOK = true
                }
            }
            "AUDIO" -> {//camera传送音频数据
                audio(event.getPcm()!!)
            }
            "DEVICE" -> {//manager service 传送设备状态 播报信息
                getDeviceMsg(event.getType()!!, event.getMsg()!!)
            }
            "CHECK_INIT" -> {
                val handler = Handler()
                handler.postDelayed({
                    DDS.getInstance().release()
                    init()
                }, 1000)
            }
            "CHECK_AUTH" -> {
                val handler = Handler()
                handler.postDelayed({
                    DDS.getInstance().doAuth()
                }, 500)
            }
            "WAKEUP" -> {//manager service 传送设备状态 播报信息
                onBindService("")
                if (event.getSuccess()!!) {
                    assistantManager?.basicTypes(1, 1, "唤醒")
                } else {
                    assistantManager?.basicTypes(1, 0, "唤醒")
                }
            }
        }
    }

    private fun getDeviceMsg(type: Int, msg: String) {
        Log.d(TAG, "收到device信息 $type  $msg")
        when (type) {
            1 -> {
                deviceStatus = msg
                toTTS(msg)//联网
            }
            2 -> {
                deviceStatus = msg
                toTTS(msg)
            }
            3 -> {
                deviceStatus = msg
                toTTS(msg)
            }
            4 -> {//关闭唤醒
//                DDS.getInstance().agent.disableWakeup()
                if (isInitOk) {
                    DDS.getInstance().agent.avatarClick()
                    DDS.getInstance().agent.wakeupEngine.disableWakeup()
                }
                EventBus.getDefault().post(AssistantMessageEvent("WAKEUP", null, null, null, false))
                assistantStatus = false
            }
            5 -> {//打开唤醒
//                DDS.getInstance().agent.enableWakeup()
                assistantStatus = true
                if (isInitOk) {
                    DDS.getInstance().agent.wakeupEngine.enableWakeup()
                }
                EventBus.getDefault().post(AssistantMessageEvent("WAKEUP", null, null, null, true))

            }
        }
    }

    private fun toTTS(msg: String) {
        if (isInitOk && isOK) {
            DDS.getInstance().agent.ttsEngine.speak(msg, 3, "100", AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
        }
    }

    private fun toLog(msg: String) {
        Log.d(TAG, msg)
    }

    private fun audio(pcm: ByteArray) {
        otherAudio(pcm)
        if (!isTTSing && isNoises) {
            isNoises = false
            var pcmS: ShortArray = AudioNoise.toShortArray(pcm)
//        Log.d(TAG, "PCM src dest = ${Arrays.toString(pcmS)}")
            var size: Int = pcmS.size
            var noise: Int = AudioNoise.calcDecibelLevel(pcmS, size) + 104
            Log.d(TAG, "the noise is $noise now")
            onBindService("")
            assistantManager?.basicTypes(0, noise, "语音")
            val handler = Handler()
            handler.postDelayed({
                isNoises = true
            }, 1000)
        }
    }

    private fun otherAudio(pcm: ByteArray) {
        if (isInitOk && isDoAuth && isOK) {
            if (TYPE_MODE_AUDIO == 1) {
                DDS.getInstance().agent.feedPcm(pcm)
                return
            }
            toLog("现在是内置语音模式，请切换为外置语音模式")
            return
        }
        toLog("isInitOk $isInitOk ，isDoAuth $isDoAuth")
        if (!isInitOk && !isIniting && isRunning) {
            isIniting = true
            EventBus.getDefault().post(AssistantMessageEvent("CHECK_INIT", null, null, null, null))
            return
        }
    }

}