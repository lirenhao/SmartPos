package com.yada.smartpos.spos;

import com.newland.pos.sdk.util.BytesUtils;
import com.newland.pos.sdk.util.ISO8583;
import com.newland.pos.sdk.util.ISO8583Exception;
import com.payneteasy.tlv.HexUtil;
import com.yada.sdk.packages.PackagingException;
import com.yada.sdk.packages.transaction.IMessage;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Arrays;

class SposMessage implements IMessage {

    private ISO8583 iso8583;
    private ByteBuffer tpduId;
    private ByteBuffer tpduToAddress;
    private ByteBuffer tpduFromAddress;
    private ByteBuffer version;
    private String pack;

    SposMessage(ISO8583 iso8583) {
        this.iso8583 = iso8583;
    }

    @Override
    public boolean hasField(int fieldId) {
        return !iso8583.getField(fieldId).isEmpty();
    }

    @Override
    public ByteBuffer getField(int fieldId) {
        return ByteBuffer.wrap(BytesUtils.getBytes(iso8583.getField(fieldId)));
    }

    @Override
    public String getFieldString(int fieldId) {
        return iso8583.getField(fieldId);
    }

    @Override
    public void setField(int fieldId, ByteBuffer fieldValue) throws PackagingException {
        setFieldString(fieldId, BytesUtils.bytesToHex(fieldValue.array()));
    }

    @Override
    public void setFieldString(int fieldId, String fieldString) throws PackagingException {
        iso8583.setField(fieldId, fieldString);
    }

    @Override
    public int getFieldMaxLen(int fieldId) {
        return iso8583.getField(fieldId).length();
    }

    @Override
    public String getTranId() {
        StringBuilder sb = new StringBuilder();
        String termId = iso8583.getField(41);
        String field61 = iso8583.getField(61);
        String batchNo = field61.substring(0, 6);
        String traceNo = iso8583.getField(11);
        String mti = iso8583.getField(0);
        return sb.append("spos").append(termId).append(batchNo).append(traceNo).append(mti).toString();
    }

    public ByteBuffer getTpduFromAddress() {
        return tpduFromAddress;
    }

    public ByteBuffer getTpduToAddress() {
        return tpduToAddress;
    }

    public void setTpduId(ByteBuffer tpduId) {
        this.tpduId = ByteBuffer.wrap(tpduId.array());
    }

    public void setVersion(ByteBuffer version) {
        this.version = ByteBuffer.wrap(version.array());
    }

    public void setTpduFromAddress(ByteBuffer tpduFromAddress) {
        this.tpduFromAddress = ByteBuffer.wrap(tpduFromAddress.array());
    }

    public void setTpduToAddress(ByteBuffer tpduToAddress) {
        this.tpduToAddress = ByteBuffer.wrap(tpduToAddress.array());
    }

    public byte[] pack() throws ISO8583Exception {
        int len = tpduId.array().length + tpduToAddress.array().length
                + tpduFromAddress.array().length + version.array().length
                + iso8583.packBytes().length;

        ByteBuffer byteBuffer = ByteBuffer.allocate(len);
        byteBuffer.put(tpduId.array());
        byteBuffer.put(tpduToAddress.array());
        byteBuffer.put(tpduFromAddress.array());
        byteBuffer.put(version.array());
        byteBuffer.put(iso8583.packBytes());
        pack = HexUtil.toHexString(byteBuffer.array());
        return byteBuffer.array();
    }

    void unpack(byte[] bytes) throws ISO8583Exception, UnsupportedEncodingException {
        tpduId = ByteBuffer.wrap(Arrays.copyOfRange(bytes, 0, 1));
        tpduToAddress = ByteBuffer.wrap(Arrays.copyOfRange(bytes, 1, 3));
        tpduFromAddress = ByteBuffer.wrap(Arrays.copyOfRange(bytes, 3, 5));
        version = ByteBuffer.wrap(Arrays.copyOfRange(bytes, 5, 7));
        pack = HexUtil.toHexString(bytes);
        iso8583.unpack(Arrays.copyOfRange(bytes, 7, bytes.length));
    }

    @Override
    public String toString() {
        return pack;
    }
}
