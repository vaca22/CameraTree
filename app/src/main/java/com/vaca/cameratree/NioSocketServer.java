package com.vaca.cameratree;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

//服务器
public class NioSocketServer {
    // 通道管理器
    private Selector selector;

    // 检测时间
    private int detect = 10000;
    // 客户端连接监听
    private OnConnectionListener onConnectedListener;

    public void setOnConnectedListener(OnConnectionListener onConnectedListener) {
        this.onConnectedListener = onConnectedListener;
    }

    // 存放客户端通道(用于发消息)
    Map<String, SocketChannel> map = null;
    // 单例
    private static NioSocketServer nioSocketServer;

    private NioSocketServer() {
        map = new HashMap<String, SocketChannel>();
    }

    public static NioSocketServer getInit() {
        if (nioSocketServer == null) {
            nioSocketServer = new NioSocketServer();
        }
        return nioSocketServer;
    }

    public void init(int port) throws IOException {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // 获取ServerSocketChannel通道
                    ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
                    // 非阻塞模式
                    serverSocketChannel.configureBlocking(false);
                    ServerSocket socket = serverSocketChannel.socket();
                    socket.bind(new InetSocketAddress("0.0.0.0", port));
                    // 获取通道管理器
                    selector = Selector.open();
                    // 将通道和通道管理器绑定，并为通道注册SelectionKey.OP_ACCEPT(服务端接收客户端连接事件，对应值为SelectionKey.OP_ACCEPT(16))
                    // 只有当该事件到达时，Selector.select()会返回，否则一直阻塞。
                    serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
                    System.out.println("服务器:" + socket.getLocalSocketAddress().toString());

                    listen();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * byteBuffer 转 byte数组
     * @param buffer
     * @return
     */
    public static byte[] bytebuffer2ByteArray(ByteBuffer buffer) {
        buffer.flip();
        int len= buffer.remaining();
        byte [] bytes=new byte[len];
        buffer.get(bytes,0,len);
        return bytes;
    }


    @SuppressWarnings("static-access")
    public void listen() throws IOException {
        while (true) {
            // 当有注册访问时
            int select = selector.select();
            if (select > 0) {
                // 获取迭代器
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    // 选中key
                    SelectionKey key = iterator.next();
                    // 删除已选key，防止重复处理
                    iterator.remove();
                    // 客户端连接事件
                    if (key.isAcceptable()) {
                        // 获取客户端通道
                        ServerSocketChannel server = (ServerSocketChannel) key.channel();
                        // 获取客户端通道连接
                        SocketChannel channel = server.accept();
                        // 非阻塞模式
                        channel.configureBlocking(false);
                        // 为客户端注册SelectionKey.OP_READ(读事件,对应值为SelectionKey.OP_READ(1))
                        channel.register(selector, SelectionKey.OP_READ);
                        InetSocketAddress inetSocketAddress = (InetSocketAddress) channel.getRemoteAddress();
                        String host = inetSocketAddress.getHostString();
                        int port = inetSocketAddress.getPort();
                        onConnectedListener.onConnected(host + ":" + port);
                        map.put(host + ":" + port, channel);
                        //设置和手机的心跳包
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                while (true) {
                                    try {
                                        Thread.sleep(detect);
                                        channel.write(ByteBuffer.wrap(new String("server_connection").getBytes()));
                                    } catch (Exception e) {
                                        try {
                                            channel.close();
                                            map.remove(host + ":" + port);
                                        } catch (Exception e1) {
                                            e1.printStackTrace();
                                        }
                                        break;
                                    }
                                }
                            }
                        }).start();
                    } else if (key.isReadable()) {// 客户端数据
                        InetSocketAddress inetSocketAddress = null;
                        SocketChannel channel = null;
                        String host = null;
                        int port = 0;
                        try {
                            // 获取客户端通道连接，读取数据
                            channel = (SocketChannel) key.channel();
                            // 读取数据缓冲器
                            ByteBuffer buffer = ByteBuffer.allocate(1024);
                            inetSocketAddress = (InetSocketAddress) channel.getRemoteAddress();
                            host = inetSocketAddress.getHostString();
                            port = inetSocketAddress.getPort();
                            int count = channel.read(buffer);
                            if (count < 0) {
                                // 客户端正常关闭
                                channel.close();
                                onConnectedListener.onDisconnected(host + ":" + port);
                                map.remove(host + ":" + port);
                                break;
                            }
                            byte[] data = bytebuffer2ByteArray(buffer);
                            buffer.clear();
                            onConnectedListener.onReceivedMessage(port, data);
                        } catch (Exception e) {
                            // 客户端非正常关闭
                            channel.close();
                            onConnectedListener.onDisconnected(host + ":" + port);
                            map.remove(host + ":" + port);
                            break;
                        }
                    }
                }
            }
        }
    }

    public String sendMessage(String address, String msg) {
        SocketChannel channel = map.get(address);
        if (channel == null) {
            return "客户端未连接！"+address;
        }
        if (channel.isConnected()) {
            try {
                channel.write(ByteBuffer.wrap(new String(msg).getBytes()));
                return "发送成功！"+address;
            } catch (IOException e) {
                e.printStackTrace();
                return "发送失败！与客户端通道异常！"+address;
            }
        } else {
            return "客户端未开启！"+address;
        }
    }

    public interface OnConnectionListener {
        void onConnected(String address);

        void onDisconnected(String address);

        void onReceivedMessage(int port, byte[] msg);
    }
}
