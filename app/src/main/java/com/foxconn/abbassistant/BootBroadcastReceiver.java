package com.foxconn.abbassistant;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by xiao-jie.qin@mail.foxconn.com on 2018/4/12.
 */

public class BootBroadcastReceiver extends BroadcastReceiver {

    private static String TAG = "ass:BootBroadcastReceiver";

    public final static String ACTION_BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";

    public final static String ACTION_REBOOT_COMPLETED = "android.intent.action.REBOOT_ASS_SERVER";

    public BootBroadcastReceiver() {
        super();
    }

    // 重写onReceive方法
    @SuppressLint("NewApi")
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ACTION_BOOT_COMPLETED)) {
            // 启动应用，参数为需要自动启动的应用的包名
            Intent serIntent = new Intent(context, AssistantService.class);
            serIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startForegroundService(serIntent);
            Log.v(TAG, "start");
        } else if (intent.getAction().equals(ACTION_REBOOT_COMPLETED)) {
            // 启动应用，参数为需要自动启动的应用的包名
            Intent serIntent = new Intent(context, AssistantService.class);
            serIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startForegroundService(serIntent);
            Log.v(TAG, "reStart");
        }
    }
}
