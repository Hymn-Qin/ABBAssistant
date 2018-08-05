package com.foxconn.abbassistant

/**
 * Created by xiao-jie.qin@mail.foxconn.com on 2018/5/24.
 */
class AssistantMessageEvent {
    private var message: String? = null
    private var type : Int? = 0
    private var pcm : ByteArray? = null
    private var success : Boolean? = false
    private var msg: String? = null

    constructor(sensorMessage: String?, type: Int?, msg: String?, pcm: ByteArray?, success: Boolean?) {
        message = sensorMessage
        this.type = type
        this.msg = msg
        this.pcm = pcm
        this.success = success
    }

    fun getMessage(): String? {
        return message
    }
    fun getType() : Int? {
        return type
    }
    fun getMsg(): String? {
        return msg
    }
    fun getPcm(): ByteArray? {
        return pcm
    }
    fun getSuccess(): Boolean? {
        return success
    }
}