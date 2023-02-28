package com.huantek.vein.util;


import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class TestPort {


    /**
     * 测试端口是否被占用
     *
     * @param port
     * @return
     */
    public boolean isLocalePortUsing(int port) {
        boolean flag = false;
        try {
            flag = testPort("127.0.0.1", port);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 测试端口是否被占用
     *
     * @param port
     * @return
     */
    public boolean isLocalePortUsingUDP(int port) {
        boolean flag = false;
        try {
            DatagramSocket socket = new DatagramSocket(port);
            socket.close();
            return flag;
        } catch (Exception e) {
            return true;//被占用
        }
    }

    private static boolean testPort(String host, int port) throws UnknownHostException {
        boolean flag = false;
        InetAddress theAddress = InetAddress.getByName(host);
        try {
            new Socket(theAddress, port);
            flag = true;
        } catch (IOException e) {

        }
        return flag;
    }
}
