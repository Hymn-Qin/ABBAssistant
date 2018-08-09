package com.foxconn.abbassistant


/**
 * Created by holywon on 2018/5/2.
 */

object AssistantData {
    //测试 b99a7c884ad613d84285cc1c5b61678f 产品ID 278571928
    //正式 823c0ce3c6864d51deb39fa35b6ad45d 产品ID 278571963
    const val API_KEY = "823c0ce3c6864d51deb39fa35b6ad45d"
    const val PRODUCT_ID = "278571963"
    //测试
    const val API_KEYS = "b99a7c884ad613d84285cc1c5b61678f"
    const val PRODUCT_IDS = "278571928"
    var USER_ID = "ABB_UNKNOWN"//用户ID
    const val ALIAS_KEY = "prod"//分支 prod  test

    /*home.control 家电控制*/
    val COMMAND_VOICE = arrayOf("home.control")
    val COMMAND_MESSAGE = arrayOf("sys.dialog.start", "sys.resource.updated", "sys.wakeup.result", "sys.dialog.start", "sys.dialog.end")

    val WAKEUP_VOICE = arrayOf("你好小安")
    val WAKEUP_VOICE_PIN = arrayOf("ni hao xiao an")
    val WAKEUP_VOICE_PIN2 = arrayOf("0.120")

    val TTS_ERROR = "{\n" +
            "\"71304\":\"\",\n" +
            "\"71305\":\"\",\n" +
            "\"71308\":\"进入闲聊模式\",\n" +
            "\"71309\":\"\"\n" +
            "}"
    var TYPE_MODE_AUDIO = 1

    var UUID_PATH = "proc/tutkuuid"

    var isOK: Boolean = true

    var isInitOk: Boolean = false//初始化状态
    var isDoAuth: Boolean = false//授权状态
    var isRunning: Boolean = false//运行状态
    var isUpdated: Boolean = false//更新状态

    var isIniting: Boolean = false//是否正在初始化
    var isAuthing: Boolean = false//是否正在授权

    var isTTSing: Boolean = false//是否正在播报

    var isNoises: Boolean = true//是否正在播报

    var assistantStatus: Boolean = true//唤醒状态

    var deviceStatus: String = ""


}
