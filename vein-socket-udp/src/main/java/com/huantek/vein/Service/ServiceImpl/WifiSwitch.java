package com.huantek.vein.Service.ServiceImpl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.huantek.vein.socket.SocketServer;
import com.huantek.vein.util.OrderBase;
import com.huantek.vein.util.PublicVariable;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class WifiSwitch {

    DatagramSocket socket;

    public WifiSwitch(DatagramSocket socket) {
        this.socket = socket;
    }

    public void serviceWIFI(JSONObject jsonObject) throws IOException {
        String action = jsonObject.getString("action");
        if (action != null || !"".equals(action)) {
            if ("updateSingleWIFI".equals(action)) updateSingleWIFI(jsonObject);
            if ("updateSuitWIFI".equals(action)) updateSuitWIFI(jsonObject);
            if ("clearSuitWIFI".equals(action)) clearSuitWIFI(jsonObject);
        } else {
            log.debug("参数未传入");
        }
    }

    //清除wifi
    private void clearSuitWIFI(JSONObject jsonObject) {
        String suit = jsonObject.getString("productID");
        JSONArray macs = jsonObject.getJSONArray("MAC");
        try {
            if (suit != null && !suit.equals("") && macs != null) {//Vein套装参数不为NULL就执行关闭LED命令
                for (Object mac : macs) {
                    String socketName = suit + "_" + mac;//拼接socket对应Key
                    String packetName = SocketServer.markAndPacket.get(socketName);//获取对应节点的socket
                    String[] split = packetName.split(":");
                    String dataOne = split[0];
                    String addressStr = dataOne.substring(1);
                    InetAddress address = InetAddress.getByName(addressStr);
                    String port = split[1];
                    //wifi清除命令
                    if (socket != null) {
                        socket.send(new DatagramPacket(OrderBase.CLEAR_WIFI, OrderBase.CLEAR_WIFI.length, address, Integer.parseInt(port)));
                    }
                }
            }
            log.debug("清除WIFI指令已发送");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //切换单个节点wifi
    private void updateSuitWIFI(JSONObject jsonObject) throws IOException {
        String productID = jsonObject.getString("productID");
        String wifiName = jsonObject.getString("wifiName");
        String wifiPassword = jsonObject.getString("wifiPassword");
        JSONArray macs = jsonObject.getJSONArray("MAC");
        for (Object mac : macs) {
            String socketName = productID + "_" + mac;
            updateWIFI(socketName, wifiName, wifiPassword);
        }
    }

    //切换整套节点wifi
    private void updateSingleWIFI(JSONObject jsonObject) throws IOException {
        String productID = jsonObject.getString("productID");
        String mac = jsonObject.getString("MAC");
        String wifiName = jsonObject.getString("wifiName");
        String wifiPassword = jsonObject.getString("wifiPassword");
        String socketName = productID + "_" + mac;
        updateWIFI(socketName, wifiName, wifiPassword);
    }

    private void updateWIFI(String socketName, String wifiName, String wifiPassword) throws IOException {
        String packetName = SocketServer.markAndPacket.get(socketName);//获取对应节点的socket
        String[] split = packetName.split(":");
        String dataOne = split[0];
        String addressStr = dataOne.substring(1);
        InetAddress address = InetAddress.getByName(addressStr);
        String port = split[1];
        if (socket != null) {
            byte[] wifiNameArr = ByteZeroFill(wifiName.getBytes(), 32);
            byte[] wifiPasswordArr = ByteZeroFill(wifiPassword.getBytes(), 64);
            byte len = (byte) (wifiNameArr.length + wifiPasswordArr.length);
            byte[] head = {0x47, 0x07, len};
            ByteArrayOutputStream byteArr = new ByteArrayOutputStream();
            byteArr.write(head, 0, head.length);
            byteArr.write(wifiNameArr, 0, wifiNameArr.length);
            byteArr.write(wifiPasswordArr, 0, wifiPasswordArr.length);
            byteArr.write(0x74);
            byte[] bytes = byteArr.toByteArray();
            socket.send(new DatagramPacket(bytes, bytes.length, address, Integer.parseInt(port)));
            log.debug("------设备切换WIFI:---" + wifiName + "--命令已发送");
        }

    }


    /**
     * 数组补零
     *
     * @param bytes 需要补零的数组
     * @param len   最终长度
     * @return
     */
    public static byte[] ByteZeroFill(byte[] bytes, int len) {
        if (bytes.length < len) {
            byte[] arr = new byte[len];
            for (int i = 0; i < bytes.length; i++) {
                arr[i] = bytes[i];
            }
            return arr;
        } else {
            return bytes;
        }
    }
}
