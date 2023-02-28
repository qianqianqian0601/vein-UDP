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
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

//设备控制
@Slf4j
public class DMClass {

    DatagramSocket socket;

    public DMClass(DatagramSocket socket) {
        this.socket = socket;
    }

    //根据参数调用应方法
    public void deviceManagement(JSONObject jsonObject) {
        String action = jsonObject.getString("action");
        if (action == null || action.equals("")) {
            log.debug("参数未传入");
        } else {
            if ("offNode".equals(action)) powerOffNode(jsonObject);//单个节点关机
            if ("offAll".equals(action)) powerOffAll();//全部关机
            if ("offSuit".equals(action)) powerOffSuit(jsonObject);//整套节点关机
            if ("rebootNode".equals(action)) powerRebootNode(jsonObject);//单个节点重启
        }
    }


    /**
     * 重启
     */

    //单个节点重启
    private void powerRebootNode(JSONObject jsonObject) {
        String suit = jsonObject.getString("suit");
        String mac = jsonObject.getString("MAC");
        if (suit.equals("") || suit == null) log.debug("套装参数未传入");
        else if (mac.equals("") || mac == null) log.debug("节点参数未传入");
        else {
            try {
                String socketName = suit + "_" + mac;
                String packetName = SocketServer.markAndPacket.get(socketName);
                String[] split = packetName.split(":");
                String dataOne = split[0];
                String addressStr = dataOne.substring(1);
                InetAddress address = InetAddress.getByName(addressStr);
                String port = split[1];
                if (socket != null) {
                    DatagramPacket sendReBoot = new DatagramPacket(OrderBase.REBOOT_COM, OrderBase.REBOOT_COM.length, address, Integer.parseInt(port));
                    socket.send(sendReBoot);
//                        if (socketMap.containsKey(socketName)){
//                            socketMap.remove(socketName,socket);
//                        }
                    log.debug("重启指令已发送");
                } else {
                    log.debug("节点未连接");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 关机
     */

    //整套设备节点关机
    private void powerOffSuit(JSONObject jsonObject) {
        String suit = jsonObject.getString("suit");//Vein套装ID
        JSONArray macs = jsonObject.getJSONArray("MAC");//Vein节点数组
        String handySuit = jsonObject.getString("handySuit");//handy套装ID
        JSONArray handyMACS = jsonObject.getJSONArray("handyMAC");//handy节点数组
        try {
            if (suit != null && !suit.equals("") && macs != null) {//Vein套装参数不NULL执行Vein关闭
                for (Object mac : macs) {
                    String VeinSocketName = suit + "_" + mac;//拼接Vein的socket节点Name
                    String packetName = SocketServer.markAndPacket.get(VeinSocketName);//获取对应的Socket
                    String[] split = packetName.split(":");
                    String dataOne = split[0];
                    String addressStr = dataOne.substring(1);
                    InetAddress address = InetAddress.getByName(addressStr);
                    String port = split[1];
                    if (socket != null) {
                        //发送关机指令
                        DatagramPacket sendOff = new DatagramPacket(OrderBase.OFF_COM, OrderBase.OFF_COM.length, address, Integer.parseInt(port));
                        socket.send(sendOff);
//                            if (socketMap.containsKey(VeinSocketName)){
//                                socketMap.remove(VeinSocketName,socket);
//                            }
                    }
                }
                log.debug("关机指令已发送");
            }
            if (handySuit != null && !handySuit.equals("") && handyMACS != null) {//handy套装参数不NULL执行handy关闭
                for (Object handyMAC : handyMACS) {
                    String handySocketName = handySuit + "_" + handyMAC;//拼接handy的socket节点Name
                    String packetName = SocketServer.markAndPacket.get(handySocketName);//获取对应的Socket
                    String[] split = packetName.split(":");
                    String dataOne = split[0];
                    String addressStr = dataOne.substring(1);
                    InetAddress address = InetAddress.getByName(addressStr);
                    String port = split[1];
                    if (socket != null) {
                        //发送关机指令
                        DatagramPacket sendOff = new DatagramPacket(OrderBase.OFF_COM, OrderBase.OFF_COM.length, address, Integer.parseInt(port));
                        socket.send(sendOff);
//                            if (socketMap.containsKey(handySocketName)){
//                                socketMap.remove(handySocketName,socket);
//                            }
                    }
                    log.debug("关机指令已发送");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //设备单个节点关机
    private void powerOffNode(JSONObject jsonObject) {
        String suit = jsonObject.getString("suit");
        String mac = jsonObject.getString("MAC");
        if (suit.equals("") || suit == null) log.debug("未指定套装");
        else if (mac.equals("") || mac == null) log.debug("未指定节点");
        else {
            try {
                String socketName = suit + "_" + mac;
                String packetName = SocketServer.markAndPacket.get(socketName);//获取对应的Socket
                String[] split = packetName.split(":");
                String dataOne = split[0];
                String addressStr = dataOne.substring(1);
                InetAddress address = InetAddress.getByName(addressStr);
                String port = split[1];
                if (socket != null) {
                    DatagramPacket sendOff = new DatagramPacket(OrderBase.OFF_COM, OrderBase.OFF_COM.length, address, Integer.parseInt(port));
                    socket.send(sendOff);
//                        if (socketMap.containsKey(socketName)){
//                            socketMap.remove(socketName,socket);
//                        }
                    log.debug("关机指令已发送");
                } else {
                    log.debug("节点未连接");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    //关闭所有设备
    public void powerOffAll() {
        try {
            if (!SocketServer.markAndPacket.isEmpty()) {
                Iterator<Map.Entry<String, String>> iterator = SocketServer.markAndPacket.entrySet().iterator();
                while (iterator.hasNext()) {
                    String packetName = iterator.next().getValue();
                    String[] split = packetName.split(":");
                    String dataOne = split[0];
                    String addressStr = dataOne.substring(1);
                    InetAddress address = InetAddress.getByName(addressStr);
                    String port = split[1];
                    DatagramPacket sendOff = new DatagramPacket(OrderBase.OFF_COM, OrderBase.OFF_COM.length, address, Integer.parseInt(port));
                    socket.send(sendOff);
                    iterator.remove();
                }
                log.debug("关机指令已发送");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
