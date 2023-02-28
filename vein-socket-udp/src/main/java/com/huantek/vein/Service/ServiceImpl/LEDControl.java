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
import java.util.concurrent.ConcurrentHashMap;

//LED控制
@Slf4j
public class LEDControl {

    DatagramSocket socket;

    public LEDControl(DatagramSocket socket) {
        this.socket = socket;
    }

    //判断传入参数调用对应方法
    public void ledService(JSONObject jsonObject) {
        String action = jsonObject.getString("action");
        if (action != null || !action.equals("")) {
            if ("ledOffNode".equals(action)) ledOffNode(jsonObject);//关闭单个节点的LED
            if ("ledOffSuit".equals(action)) ledOffSuit(jsonObject);//关闭整套节点的LED
            if ("ledOffAll".equals(action)) ledOffAll();//关闭所有节点的LED
            if ("ledOpenNode".equals(action)) ledOpenNode(jsonObject);//打开单个节点的LED
            if ("ledOpenSuit".equals(action)) ledOpenSuit(jsonObject);//打开整套节点的LED
            if ("ledOpenAll".equals(action)) ledOpenAll();//打开所有节点的LED
            if ("ledFlashNode".equals(action)) ledFlashNode(jsonObject);//闪烁单个节点的LED
        } else {
            log.debug("参数未传入");
        }
    }

    /**
     * 闪烁LED
     */

    //寻找节点：闪烁三次
    private void ledFlashNode(JSONObject jsonObject) {
        String suit = jsonObject.getString("suit");
        String mac = jsonObject.getString("MAC");
        if (suit.equals("") || suit == null) log.debug("未传入套装参数");
        else if (mac.equals("") || mac == null) log.debug("未传入MAC参数");
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
                    DatagramPacket send = new DatagramPacket(OrderBase.LED_FLASH, OrderBase.LED_FLASH.length, address, Integer.parseInt(port));
                    socket.send(send);
                    log.debug("LED闪烁指令已发送");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 打开LED
     */

    //打开所有节点的LED
    private void ledOpenAll() {
        try {
            if (!SocketServer.markAndPacket.isEmpty()) {
                int count = 0;
                for (String packetName : SocketServer.markAndPacket.values()) {
                    String[] split = packetName.split(":");
                    String dataOne = split[0];
                    String addressStr = dataOne.substring(1);
                    InetAddress address = InetAddress.getByName(addressStr);
                    String port = split[1];
                    count++;
                    log.debug(count + "::::" + socket.getPort());
                    //发送节点LED打开指令
                    DatagramPacket send = new DatagramPacket(OrderBase.LED_OPEN, OrderBase.LED_OPEN.length, address, Integer.parseInt(port));
                    socket.send(send);
                }
            }
            log.debug("LED打开指令已发送");
        } catch (Exception e) {
            log.debug("LED打开error");
            e.printStackTrace();
            log.debug("LED打开error");
        }
    }

    //打开整套节点的LED
    private void ledOpenSuit(JSONObject jsonObject) {
        String suit = jsonObject.getString("suit");
        JSONArray macs = jsonObject.getJSONArray("MAC");
        String handySuit = jsonObject.getString("handySuit");
        JSONArray handyMACs = jsonObject.getJSONArray("handyMAC");
        try {
            if (suit != null && !suit.equals("") && macs != null) {//Vein套装参数不为NULL就执行关闭LED命令
                for (Object mac : macs) {
                    String socketName = suit + "_" + mac;//拼接socket对应Key
                    String packetName = SocketServer.markAndPacket.get(socketName);//获取对应节点socket
                    String[] split = packetName.split(":");
                    String dataOne = split[0];
                    String addressStr = dataOne.substring(1);
                    InetAddress address = InetAddress.getByName(addressStr);
                    String port = split[1];
                    if (socket != null) {
                        socket.send(new DatagramPacket(OrderBase.LED_OPEN, OrderBase.LED_OPEN.length, address, Integer.parseInt(port)));
                    }//发送打开LED指令
                }
            }
            if (handySuit != null && !handySuit.equals("") && handyMACs != null) {//Handy套装参数不为NULL就执行关闭LED命令
                for (Object handyMAC : handyMACs) {
                    String socketName = handySuit + "_" + handyMAC;//拼接socket对应Key
                    String packetName = SocketServer.markAndPacket.get(socketName);//获取对应节点socket
                    String[] split = packetName.split(":");
                    String dataOne = split[0];
                    String addressStr = dataOne.substring(1);
                    InetAddress address = InetAddress.getByName(addressStr);
                    String port = split[1];
                    //发送打开LED指令
                    if (socket != null) {
                        socket.send(new DatagramPacket(OrderBase.LED_OPEN, OrderBase.LED_OPEN.length, address, Integer.parseInt(port)));//发送打开LED指令
                    }
                }
            }
            log.debug("LED打开指令已发送");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //打开单个节点的LED
    private void ledOpenNode(JSONObject jsonObject) {
        String suit = jsonObject.getString("suit");
        String mac = jsonObject.getString("MAC");
        if (suit.equals("") || suit == null) log.debug("未传入套装参数");
        else if (mac.equals("") || mac == null) log.debug("未传入MAC参数");
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
                    //发送节点LED打开指令
                    DatagramPacket send = new DatagramPacket(OrderBase.LED_OPEN, OrderBase.LED_OPEN.length, address, Integer.parseInt(port));
                    socket.send(send);
                    log.debug("LED打开指令已发送");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 关闭LED
     */

    //关闭全部节点的LED
    private void ledOffAll() {
        int count = 0;
        try {
            if (!SocketServer.markAndPacket.isEmpty()) {
                for (String packetName : SocketServer.markAndPacket.values()) {
                    String[] split = packetName.split(":");
                    String dataOne = split[0];
                    String addressStr = dataOne.substring(1);
                    InetAddress address = InetAddress.getByName(addressStr);
                    String port = split[1];
                    count++;
                    log.debug(count + "::::" + socket.getPort());
                    DatagramPacket send = new DatagramPacket(OrderBase.LED_OFF, OrderBase.LED_OFF.length, address, Integer.parseInt(port));
                    socket.send(send);
                }
            }
            log.debug("LED关闭指令已发送");
        } catch (Exception e) {

            log.debug(count + "LED关闭error");
            e.printStackTrace();
            log.debug("LED关闭error");
        }
    }

    //整套设备LED关闭
    private void ledOffSuit(JSONObject jsonObject) {
        String suit = jsonObject.getString("suit");
        JSONArray macs = jsonObject.getJSONArray("MAC");
        String handySuit = jsonObject.getString("handySuit");
        JSONArray handyMACs = jsonObject.getJSONArray("handyMAC");
        try {
            if (suit != null && !suit.equals("") && macs != null) {//Vein套装参数不为NULL就执行关闭LED命令
                for (Object mac : macs) {
                    String socketName = suit + "_" + mac;//拼接socket对应Key
                    String packetName = SocketServer.markAndPacket.get(socketName);//获取对应节点socket
                    String[] split = packetName.split(":");
                    String dataOne = split[0];
                    String addressStr = dataOne.substring(1);
                    InetAddress address = InetAddress.getByName(addressStr);
                    String port = split[1];
                    //发送关闭LED指令
                    if (socket != null) {
                        socket.send(new DatagramPacket(OrderBase.LED_OFF, OrderBase.LED_OFF.length, address, Integer.parseInt(port)));
                    }
                }
            }
            if (handySuit != null && !handySuit.equals("") && handyMACs != null) {//Handy套装参数不为NULL就执行关闭LED命令
                for (Object handyMAC : handyMACs) {
                    String socketName = handySuit + "_" + handyMAC;//拼接socket对应Key
                    String packetName = SocketServer.markAndPacket.get(socketName);//获取对应节点socket
                    String[] split = packetName.split(":");
                    String dataOne = split[0];
                    String addressStr = dataOne.substring(1);
                    InetAddress address = InetAddress.getByName(addressStr);
                    String port = split[1];
                    //发送关闭LED指令
                    if (socket != null) {
                        socket.send(new DatagramPacket(OrderBase.LED_OFF, OrderBase.LED_OFF.length, address, Integer.parseInt(port)));
                    }

                }
            }
            log.debug("LED关闭指令已发送");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //单个节点LED关闭
    private void ledOffNode(JSONObject jsonObject) {
        String suit = jsonObject.getString("suit");
        String mac = jsonObject.getString("MAC");
        if (suit.equals("") || suit == null) log.debug("未传入套装参数");
        else if (mac.equals("") || mac == null) log.debug("未传入MAC参数");
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
                    DatagramPacket send = new DatagramPacket(OrderBase.LED_OFF, OrderBase.LED_OFF.length, address, Integer.parseInt(port));
                    socket.send(send);
                    log.debug("LED关闭指令已发送");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
