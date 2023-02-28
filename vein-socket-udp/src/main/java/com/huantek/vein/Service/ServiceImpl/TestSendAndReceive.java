package com.huantek.vein.Service.ServiceImpl;

import com.alibaba.fastjson.JSONObject;
import com.huantek.vein.socket.SocketServer;
import com.huantek.vein.util.PublicVariable;
import lombok.extern.slf4j.Slf4j;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

@Slf4j
public class TestSendAndReceive {
    private byte[] arr = {0x47, 0x0B, 0x01, 0x00, 0x74};//测试通信时长
    DatagramSocket socket;

    public TestSendAndReceive(DatagramSocket socket) {
        this.socket = socket;
    }

    public void testCommunicationService(JSONObject jsonObject) {
        String action = jsonObject.getString("action");
        if (action == null || action.equals("")) {
            log.debug("参数未传入");
        } else {
            if ("testFirmwareSendAndReceive".equals(action)) testFirmwareSendAndReceive();//测试固件通信时长
        }
    }

    private void testFirmwareSendAndReceive() {
        try {
            if (!PublicVariable.socketMap.isEmpty()) {
                for (String packetName : SocketServer.markAndPacket.values()) {
                    //发送测试通信命令
                    String[] split = packetName.split(":");
                    String dataOne = split[0];
                    String addressStr = dataOne.substring(1);
                    InetAddress address = InetAddress.getByName(addressStr);
                    String port = split[1];
                    DatagramPacket send = new DatagramPacket(arr, arr.length, address, Integer.parseInt(port));
                    socket.send(send);
                    log.debug("发送测试通信命令");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
