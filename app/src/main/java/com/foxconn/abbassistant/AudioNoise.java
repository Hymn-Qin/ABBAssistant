package com.foxconn.abbassistant;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;

public class AudioNoise {

    private static String TAG = "AudioNoise";

    private static int mVolume = 0;

    public static int calcDecibelLevel(short[] buffer, int readSize) {
        double sum = 0;
        short trackMaxAmplitude = 0;

        for (short rawSample : buffer) {
            double sample = rawSample / 32768.0;
            sum += sample * sample;
        }


        double rms = Math.sqrt(sum / readSize);
        final double db = 20 * Math.log10(rms);

        mVolume = (int) db;
        return mVolume;
    }

    public static short[] toShortArray(byte[] src) {
        int count = src.length >> 1;
        short[] dest = new short[count];
//        Log.d(TAG, "toShortArray:pcm = " + Arrays.toString(src));
        for (int i = 0; i < count; i++) {
            dest[i] = (short) (((src[i * 2 + 1] & 0x00ff) << 8) | (src[2 * i] & 0x00ff));
            if( (src[i * 2 + 1 ] & 0x0080) == 0x0080) {
                dest[i] = (short) (~(dest[i]) +1);
            }
        }
        return dest;
    }

    public static byte[] toByteArray(short[] src) {

        int count = src.length;
        byte[] dest = new byte[count << 1];
        for (int i = 0; i < count; i++) {
            dest[i * 2] = (byte) (src[i] >> 8);
            dest[i * 2 + 1] = (byte) (src[i] >> 0);
        }

        return dest;
    }


    public static String getID(String filename) {
        Log.d(TAG, "come to read file");
        File file = new File(filename);
        Log.d(TAG, "file = " + file);
        BufferedReader reader = null;
        StringBuilder sb = new StringBuilder();
        try {
            reader = new BufferedReader(new FileReader(file));
            String result = "";
            while ((result = reader.readLine()) != null) {
                if (result != null && result.length() > 4) {
                    sb.append(result);
                }
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            Log.d(TAG, "file not find");
            Log.d(TAG, "e.getStackTrace() = " + e.getStackTrace());
        } catch (IOException e) {
            Log.d(TAG, "IOException");
            Log.d(TAG, "e.getStackTrace() = " + e.getStackTrace());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }
}
