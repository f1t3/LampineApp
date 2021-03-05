package com.lampineapp.frag_configure_lamp.whiteconfig;

import android.content.Context;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class WhiteLampModesFile {

    final static private String FILE_NAME = "white_lamp_modes3";
    final static private int SOF_BYTE = (byte)0xAA;
    final static private int EOF_BYTE = (byte)0x55;
    final static private int SOC_BYTE = (byte)0x0A;
    final static private int EOC_BYTE = (byte)0x05;

    protected static void writeFile(Context context, WhiteModeConfigHolder configHolder) {
        try {
            FileOutputStream fos = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            fos.write(getBytes(configHolder));
        } catch (Exception e) {}
    }

    public static byte[] readFileBytes(Context context) {
        File file = new File(context.getFilesDir(), FILE_NAME);
        if (!file.exists()) {
            return null;
        }
        int size = (int) file.length();
        byte[] bytes = new byte[size];
        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(bytes, 0, bytes.length);
            buf.close();
        } catch (Exception e) {
            return null;
        }
        return bytes;
    }

    protected static WhiteLampMode[] readFile(Context context) {
        byte[] bytes = readFileBytes(context);
        if (bytes == null) {
            return null;
        }
        return parseBytes(bytes);
    }

    public static void erase(Context context) {
        File file = new File(context.getFilesDir(), FILE_NAME);
        if (file.exists()) {
            file.delete();
        }
    }

    private static WhiteLampMode[] parseBytes(byte[] bytes) {
        WhiteLampMode[] array = new WhiteLampMode[bytes[3]];
        int numModes = 0;
        for (int i = 4; i < bytes.length - 1; i++) {
            if (bytes[i] == SOC_BYTE) {
                array[numModes] = fromBytes(new byte[] {bytes[i+1]});
                i++;
                numModes++;
            }
        }
        return array;
    }

    public static byte[] getBytes(WhiteModeConfigHolder configHolder) {
        int numConfigs =  configHolder.getNumConfigs();

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            stream.write(SOF_BYTE);
            // Write dummy file length, insert later
            stream.write(new byte[]{0x00, 0x00});
            stream.write((byte)(numConfigs));
            for (int i = 0; i < numConfigs; i++) {
                stream.write(SOC_BYTE);
                stream.write(toBytes(configHolder.getAt(i)));
                stream.write(EOC_BYTE);
            }
            stream.write(EOF_BYTE);
        } catch (Exception e) {}
        byte[] bytes = stream.toByteArray();
        bytes[1] = (byte)(((bytes.length & 0xFF00) >> 8) & 0xFF);
        bytes[2] = (byte)(((bytes.length & 0x00FF) >> 0) & 0xFF);
        return bytes;
    }

    private static byte[] toBytes(WhiteLampMode mode) {
        return new byte[] {(byte) ((int)(255*mode.getIntensity()) & 0x00FF)};
    }

    private static WhiteLampMode fromBytes(byte[] bytes) {
        return new WhiteLampMode((float) ((bytes[0] & 0x00FF)) / 255);
    }
}
