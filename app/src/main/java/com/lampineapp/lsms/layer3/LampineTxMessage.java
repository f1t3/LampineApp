package com.lampineapp.lsms.layer3;

public class LampineTxMessage extends LampineMessage {

    public LampineTxMessage(MessageType type) {
        super(type, new byte[] {});
    }

    public LampineTxMessage(MessageType type, byte[] data) {
        super(type, data);
    }
}
