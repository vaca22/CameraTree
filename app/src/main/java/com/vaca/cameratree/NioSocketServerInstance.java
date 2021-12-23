package com.vaca.cameratree;


import android.util.Log;

import java.io.IOException;

public class NioSocketServerInstance {

    TcpReceiveMsg tcpReceiveMsg=null;

    private NioSocketServer.OnConnectionListener onConnectedListener = new NioSocketServer.OnConnectionListener() {
        @Override
        public void onConnected(String address) {
            Log.e("客户端连接:", address);
        }

        @Override
        public void onDisconnected(String address) {
            Log.e("客户端断开:", address);
        }

        @Override
        public void onReceivedMessage(int port, byte[] msg) {
            //线程处理并发
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if(tcpReceiveMsg!=null){
                        tcpReceiveMsg.receive (port,msg);
                    }

                }
            }).start();
        }
    };

    public void initServer() {
        try {
            NioSocketServer nioSocketServer = NioSocketServer.getInit();
            nioSocketServer.setOnConnectedListener(onConnectedListener);
            nioSocketServer.init(13207);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}