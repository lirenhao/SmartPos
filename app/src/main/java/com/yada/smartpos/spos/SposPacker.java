package com.yada.smartpos.spos;

import com.newland.pos.sdk.util.ISO8583;
import com.newland.pos.sdk.util.ISO8583Exception;
import com.yada.sdk.packages.PackagingException;
import com.yada.sdk.packages.transaction.IMessage;
import com.yada.sdk.packages.transaction.IPacker;
import com.yada.smartpos.activity.MainActivity;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class SposPacker implements IPacker {

    private MainActivity mainActivity;
    private byte[] head;

    public SposPacker(MainActivity mainActivity, byte[] head) {
        this.mainActivity = mainActivity;
        this.head = head;
    }

    @Override
    public ByteBuffer pack(IMessage message) throws PackagingException {
        SposMessage sposMessage = (SposMessage) message;
        try {
            return ByteBuffer.wrap(sposMessage.pack());
        } catch (ISO8583Exception e) {
            throw new PackagingException(e);
        }
    }

    @Override
    public IMessage unpack(ByteBuffer byteBuffer) throws PackagingException {
        try {
            byte[] bts = new byte[byteBuffer.remaining()];
            byteBuffer.get(bts);
            SposMessage message = (SposMessage) createEmpty();
            message.unpack(bts);
            return message;
        } catch (UnsupportedEncodingException | ISO8583Exception e) {
            throw new PackagingException(e);
        }
    }

    @Override
    public IMessage createEmpty() {
        ISO8583 iso8583 = new ISO8583(mainActivity);
        iso8583.loadXmlFile("CUPS8583.xml");

        SposMessage message = new SposMessage(iso8583);
        message.setTpduId(ByteBuffer.wrap(Arrays.copyOfRange(head, 0, 1)));
        message.setTpduToAddress(ByteBuffer.wrap(Arrays.copyOfRange(head, 1, 3)));
        message.setTpduFromAddress(ByteBuffer.wrap(Arrays.copyOfRange(head, 3, 5)));
        message.setVersion(ByteBuffer.wrap(Arrays.copyOfRange(head, 5, 7)));

        return message;
    }
}
