package com.lampineapp.lsms.layer2;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public abstract class LampineLayer2FrameAssembler implements LampineLayer2InputBuffer {

    private ByteArrayOutputStream stream = new ByteArrayOutputStream();

    // TODO: Dont use toString!!!!!
    @Override
    public void put(byte[] bytes) {
        if (isACKFrame(bytes)) {
            onACKFrameComplete();
        } else if (isNACKFrame(bytes)) {
            onNACKFrameComplete();
        } else if (beginsWithSOF(bytes) || endsWithEOF(bytes)) {
            try {stream.write(bytes);} catch (IOException e) {};
            if (beginsWithSOF(bytes) && endsWithEOF(bytes)) {
                LampineFrame frame = new LampineRxFrame(stream.toByteArray());
                stream.reset();
                onDATFrameComplete(frame);
            } else if (endsWithEOF(bytes)) {
                LampineFrame frame = new LampineRxFrame(stream.toByteArray());
                stream.reset();
                onDATFrameComplete(frame);
            }
        } else {
            try {stream.write(bytes);} catch (IOException e) {};
        }
    }

    public void clear() {
        stream.reset();
    }

    static private boolean isACKFrame(byte[] bytes) {
        if (bytes.length == 1 && bytes[0] == LampineFrame.ACK) {
            return true;
        }
        return false;
    }

    static private boolean isNACKFrame(byte[] bytes) {
        if (bytes.length == 1 && bytes[0] == LampineFrame.NACK) {
            return true;
        }
        return false;
    }

    static private boolean beginsWithSOF(byte[] bytes) {
        if (bytes[0] == LampineFrame.SOF) {
            return true;
        }
        return false;
    }

    static private boolean endsWithEOF(byte[] bytes) {
        if (bytes[bytes.length - 1] == LampineFrame.EOF) {
            return true;
        }
        return false;
    }





}
