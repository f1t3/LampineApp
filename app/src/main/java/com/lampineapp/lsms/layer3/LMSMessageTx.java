package com.lampineapp.lsms.layer3;

public class LMSMessageTx extends LMSMessage {

    public LMSMessageTx(MessageType type) {
        super(type, new byte[] {});
    }

    public LMSMessageTx(MessageType type, byte[] data) {
        super(type, data);
    }
}
