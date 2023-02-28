package com.huantek.vein.util;

import com.huantek.vein.socket.SocketServer;
import lombok.extern.slf4j.Slf4j;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;

@Slf4j
public class startSendData extends Thread {
    DatagramSocket socket;

    public startSendData(DatagramSocket socket) {
        this.socket = socket;
    }

    public void sendDataEvent() {
        new Timer().schedule(new TimerTask() {

            @Override
            public void run() {
                try {
                    //500ms没有新的连接逻辑处理
                    //500ms没有新的连接 开始发送动作数据
                    if (SocketServer.newConnectTime != 0) {
                        long timeMillis = System.currentTimeMillis();
                        long l = timeMillis - SocketServer.newConnectTime;
                        if (l > 500 && SocketServer.connectFlag == false) {
                            long count = SocketServer.packetAndNode.values().stream().filter(a -> a != -1).count();
                            if (count==SocketServer.markAndPacket.size()){
                                for (String ipAndPort : SocketServer.markAndPacket.values()) {
                                    String[] split = ipAndPort.split(":");
                                    String dataOne = split[0];
                                    String addressStr = dataOne.substring(1);
                                    InetAddress sendAddress = InetAddress.getByName(addressStr);
                                    String sendPort = split[1];
                                    //发送停止指令
                                    try {
                                        DatagramPacket send = new DatagramPacket(OrderBase.START_MOTION_CAPTURE, OrderBase.START_MOTION_CAPTURE.length, sendAddress, Integer.parseInt(sendPort));
                                        socket.send(send);
                                        log.debug(addressStr + ":开始发送数据");
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                                SocketServer.connectFlag = true;
                            }
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0, 200);
    }


}
