package com.yada.smartpos.util;

import com.newland.pos.sdk.util.BytesUtils;
import com.yada.sdk.net.TcpClient;
import com.yada.smartpos.activity.MainActivity;

import java.io.IOException;
import java.nio.ByteBuffer;

public class Client {

    private static String tpdu = "60001200001306";

    /**
     * @param client
     * @param pack        发送的交易报文
     * @param reversePack 为null表示该笔交易不冲正
     * @return
     */
    public static String send(MainActivity mainActivity, TcpClient client,
                              String pack, String reversePack) throws IOException {
        String unpack = null;
        try {
            ByteBuffer reqBuffer = ByteBuffer.wrap(BytesUtils.hexStringToBytes(tpdu + pack));
            System.out.println("刷卡8583包：" + tpdu + pack);
            client.open();
            ByteBuffer respBuffer = client.send(reqBuffer);
            unpack = BytesUtils.bytesToHex(respBuffer.array()).substring(14);
            System.out.println(unpack);
        } catch (IOException e) {
            // IO异常冲正
            e.printStackTrace();
            // 冲正报文不为空时进行冲正
            if (reversePack != null) {
                sendReverse(mainActivity, client, reversePack);
            }
            // 抛出异常
            throw e;
        } finally {
            client.close();
        }
        return unpack;
    }

    public static String sendReverse(MainActivity mainActivity, TcpClient client, String reversePack) {
        String reverseUnpack = null;
        try {
            ByteBuffer reverseReqBuffer = ByteBuffer.wrap(BytesUtils.hexStringToBytes(tpdu + reversePack));
            System.out.println("冲正8583包：" + tpdu + reversePack);
            client.open();
            ByteBuffer reverseRespBuffer = client.send(reverseReqBuffer);
            reverseUnpack = BytesUtils.bytesToHex(reverseRespBuffer.array()).substring(14);
            System.out.println(reverseUnpack);
        } catch (IOException e) {
            e.printStackTrace();
            /**
             * 冲正交易在超时发生时先冲正一笔，如果冲正未收到应答，则下次正常交易的时候再冲，
             * 如果冲正还是未收到应答，则正常交易失败，并等待下一笔正常交易的时候继续冲正，
             * 类似处理一直冲到能接到冲正应答为止
             */
            // 冲正失败保存冲正记录
            SharedPreferencesUtil.setReverseParam(mainActivity, reversePack);
        } finally {
            client.close();
        }
        return reverseUnpack;
    }

}
