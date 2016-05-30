package com.yada.smartpos;

import com.newland.pos.sdk.util.BytesUtils;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

public class SocketServerTest {

    ByteBuffer buffer = ByteBuffer.allocate(2048);

    public static void main(String[] args) {
        SocketServerTest manager = new SocketServerTest();
        manager.doListen();
    }

    public void doListen() {
        ServerSocket server;
        try {
            server = new ServerSocket(6789);
            while (true) {
                Socket client = server.accept();
                new Thread(new SSocket(client)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //服务器进程
    class SSocket implements Runnable {

        Socket client;

        public SSocket(Socket client) {
            this.client = client;
        }

        public void run() {
            try {
                byte[] recvBuffer = new byte[125];
                int tex = client.getInputStream().read(recvBuffer);
                System.out.println(BytesUtils.bytesToHex(recvBuffer));
                client.getOutputStream().write(recvBuffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
