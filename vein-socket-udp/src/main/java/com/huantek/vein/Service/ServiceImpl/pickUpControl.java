package com.huantek.vein.Service.ServiceImpl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.huantek.vein.socket.SocketServer;
import com.huantek.vein.util.OrderBase;
import com.huantek.vein.util.PublicVariable;
import lombok.extern.slf4j.Slf4j;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class pickUpControl {

    DatagramSocket socket;

    public pickUpControl(DatagramSocket socket) {
        this.socket = socket;
    }

    public void pickUpCalibration(JSONObject jsonObject) {
        String action = jsonObject.getString("action");
        if (null != action && action != "") {
            if (action.equals("pickUpAll")) pickUpAll();
            if (action.equals("pickUpNode")) pickUpNode(jsonObject);
            if (action.equals("pickUpSuit")) pickUpSuit(jsonObject);
        }
    }

    //套装传感器校准
    private void pickUpSuit(JSONObject jsonObject) {
        String suit = jsonObject.getString("productID");
        JSONArray macs = jsonObject.getJSONArray("mac");
        if (suit.equals("") || suit == null) log.debug("未传入套装参数");
        else if (macs == null) log.debug("未传入MAC参数");
        else {
            try {
                for (Object mac : macs) {
                    String socketName = suit + "_" + mac;
                    String packetName = SocketServer.markAndPacket.get(socketName);//获取对应节点socket
                    String[] split = packetName.split(":");
                    String dataOne = split[0];
                    String addressStr = dataOne.substring(1);
                    InetAddress address = InetAddress.getByName(addressStr);
                    String port = split[1];
                    if (socket != null) {
                        //发送节点LED关闭指令
                        DatagramPacket send = new DatagramPacket(OrderBase.PICKUP_CALIBRATION, OrderBase.PICKUP_CALIBRATION.length, address, Integer.parseInt(port));
                        socket.send(send);
                        log.debug("套装传感器校准已发送");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.debug("传感器校准异常");
            }
        }
    }

    //节点传感器校准
    private void pickUpNode(JSONObject jsonObject) {
        String suit = jsonObject.getString("productID");
        String mac = jsonObject.getString("mac");
        if (suit.equals("") || suit == null) log.debug("未传入套装参数");
        else if (!mac.equals("") || mac == null) log.debug("未传入MAC参数");
        else {
            try {
                String socketName = suit + "_" + mac;
                String packetName = SocketServer.markAndPacket.get(socketName);//获取对应节点socket
                String[] split = packetName.split(":");
                String dataOne = split[0];
                String addressStr = dataOne.substring(1);
                InetAddress address = InetAddress.getByName(addressStr);
                String port = split[1];
                if (socket != null) {
                    //发送节点LED关闭指令
                    DatagramPacket send = new DatagramPacket(OrderBase.PICKUP_CALIBRATION, OrderBase.PICKUP_CALIBRATION.length, address, Integer.parseInt(port));
                    socket.send(send);
                    log.debug("节点传感器校准已发送");
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.debug("传感器校准异常");
            }

        }
    }

    //所有传感器校准
    private void pickUpAll() {
        try {
            if (!SocketServer.markAndPacket.isEmpty()) {
                Iterator<Map.Entry<String, String>> iterator = SocketServer.markAndPacket.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, String> next = iterator.next();
                    String packetName = next.getValue();
                    if (socket != null) {
                        String[] split = packetName.split(":");
                        String dataOne = split[0];
                        String addressStr = dataOne.substring(1);
                        InetAddress address = InetAddress.getByName(addressStr);
                        String port = split[1];
                        //发送节点LED关闭指令
                        DatagramPacket send = new DatagramPacket(OrderBase.PICKUP_CALIBRATION, OrderBase.PICKUP_CALIBRATION.length, address, Integer.parseInt(port));
                        socket.send(send);
                        log.debug("传感器校准命令已发送");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.debug("传感器校准异常");
        }
    }
}
