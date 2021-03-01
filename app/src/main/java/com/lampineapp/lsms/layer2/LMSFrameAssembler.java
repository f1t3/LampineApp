package com.lampineapp.lsms.layer2;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public abstract class LMSFrameAssembler implements LMSFrameRxBuffer {

    private ByteArrayOutputStream stream = new ByteArrayOutputStream();
    private int mFrameLen = 0;

    // TODO: Dont use toString!!!!!
    @Override
    public void put(byte[] bytes) {
        if (isACKFrame(bytes)) {
            onACKFrameComplete();
        } else if (isNACKFrame(bytes)) {
            onNACKFrameComplete();
        } else if (beginsWithSOF(bytes)) {
            if (hasStreamExceededLen(stream))
                stream.reset();
            mFrameLen = getLen(bytes);
            try {stream.write(bytes);} catch (IOException e) {};
        } else {
            try {stream.write(bytes);} catch (IOException e) {};
        }
        if (hasStreamReachedLen(stream)) {
            LMSFrame frame = new LMSFrameRx(stream.toByteArray());
            stream.reset();
            onDATFrameComplete(frame);
        }
    }

    public void clear() {
        stream.reset();
    }

    static private boolean isACKFrame(byte[] bytes) {
        if (bytes.length == 1 && bytes[0] == LMSFrame.ACK) {
            return true;
        }
        return false;
    }

    static private boolean isNACKFrame(byte[] bytes) {
        if (bytes.length == 1 && bytes[0] == LMSFrame.NACK) {
            return true;
        }
        return false;
    }

    static private boolean beginsWithSOF(byte[] bytes) {
        if (bytes[0] == LMSFrame.SOF) {
            return true;
        }
        return false;
    }

    private boolean hasStreamReachedLen(ByteArrayOutputStream stream) {
        if (stream.size() == mFrameLen + LMSFrame.NUM_SOF_BYTES + LMSFrame.NUM_LEN_BYTES + LMSFrame.NUM_CHECKSUM_BYTES) {
            return true;
        }
        return false;
    }

    private boolean hasStreamExceededLen(ByteArrayOutputStream stream) {
        if (stream.size() > mFrameLen + LMSFrame.NUM_SOF_BYTES + LMSFrame.NUM_LEN_BYTES + LMSFrame.NUM_CHECKSUM_BYTES) {
            return true;
        }
        return false;
    }

    static private int getLen(byte[] bytes) {
        if (bytes[0] != LMSFrame.SOF) {
            return -1;
        }
        return bytes[LMSFrame.POS_LEN_BYTE];
    }

    static private boolean endsWithEOF(byte[] bytes) {
        if (bytes[bytes.length - 1] == LMSFrame.EOF) {
            return true;
        }
        return false;
    }





}
