package com.lampineapp.lamp;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

public class LampineK9RGB implements LampineHeadlamp {


    @Override
    public LampType getLampType() {
        return LampType.HEADLAMP;
    }

    @Override
    public int getFlux_lm(float percentage) {
        return (int)((percentage*10000) / 3);
    }

    @Override
    public int getLedInputPower_mW(float relIntensity) {
        return (int)(relIntensity * 3677);
    }

    @Override
    public int getLedCurrent_mA(float relIntensity) {
        return (int)(relIntensity * 2727);
    }

    @Override
    public int getBatLifetime_min(float relIntensity) {
        return (int)((1.1-relIntensity)*30);
    }

    @Override
    public int getMaxIntensityRuntime(float relIntensity) {
        if (relIntensity > 0.9) {
            return (int) ((1 - relIntensity) * 60);
        }
        return -1;
    }

    public static byte[] getMemctlSaveWhiteconfigCmd(byte[] configFile) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            String cmd = "memctl:-save#configwhite#";
            stream.write(cmd.getBytes(StandardCharsets.US_ASCII));
            stream.write(configFile);
            return stream.toByteArray();
        } catch (Exception e) {
            return null;
        }
    }

    public static byte[] getLedctlColorCmd(int r, int g, int b) {
        byte[] rgb = {(byte)(0x0FF & r), (byte)(0x0FF & g), (byte)(0x0FF & b)};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            String cmd = "ledctl:-c#";
            stream.write(cmd.getBytes(StandardCharsets.US_ASCII));
            stream.write(rgb);
            return stream.toByteArray();
        } catch (Exception e) {
            return null;
        }
    }

    public static byte[] getLedctlWhiteCmd(int intensity) {
        byte[] i = {(byte)(0x0FF &intensity)};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            String cmd = "ledctl:-w#";
            stream.write(cmd.getBytes(StandardCharsets.US_ASCII));
            stream.write(i);
            return stream.toByteArray();
        } catch (Exception e) {
            return null;
        }
    }

    public static byte[] getLedctlIntensityCmd(int intensity) {
        byte i = (byte)(0x0FF & intensity);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            String cmd = "ledctl:-i#";
            stream.write(cmd.getBytes(StandardCharsets.US_ASCII));
            stream.write(i);
            return stream.toByteArray();
        } catch (Exception e) {
            return null;
        }
    }
}
