package com.huantek.vein.Service.ServiceImpl;

import com.alibaba.fastjson.JSONObject;
import com.huantek.jni.conversionData.ConversionData;
import com.huantek.vein.socket.SocketServer;
import com.huantek.vein.util.OrderBase;
import com.huantek.vein.util.PublicVariable;
import com.huantek.vein.util.TransformUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

@Slf4j
public class frameControl {

    DatagramSocket socket;

    public frameControl(DatagramSocket socket) {
        this.socket = socket;
    }

    public void FPS(JSONObject jsonObject) throws IOException {
        String action = jsonObject.getString("action");
        if (action.equals("settingFPS")) {
            frameSetting(jsonObject);
        } else if (action.equals("queryFPS")) {
            queryFPS(jsonObject);
        }
    }

    private void queryFPS(JSONObject jsonObject) {
        String productID = jsonObject.getString("productID");
        String mac = jsonObject.getString("mac");
        if (productID.equals("") || productID == null) log.debug("未传入套装参数");
        else if (mac.equals("") || mac == null) log.debug("未传入MAC参数");
        else {
            try {
                String socketName = productID + "_" + mac;
                String packetName = SocketServer.markAndPacket.get(socketName);//获取对应节点socket
                String[] split = packetName.split(":");
                String dataOne = split[0];
                String addressStr = dataOne.substring(1);
                InetAddress address = InetAddress.getByName(addressStr);
                String port = split[1];
                if (socket != null) {
                    DatagramPacket send = new DatagramPacket(OrderBase.FPS_QUERY, OrderBase.FPS_QUERY.length, address, Integer.parseInt(port));
                    socket.send(send);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }


    public void frameSetting(JSONObject jsonObject) throws IOException {
        Integer frameTime = jsonObject.getInteger("frame");
        byte[] head = {0x47 , 0x15 , 0x04};
        byte[] bytes = TransformUtil.intToByteLittle(frameTime);
        byte[] tail = {0x74};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();//byte[]拼接
        stream.write(head);
        stream.write(bytes);
        stream.write(tail);
        byte[] byteArray = stream.toByteArray();
        if (!SocketServer.markAndPacket.isEmpty()) {
            for (String packetName : SocketServer.markAndPacket.values()) {
                String[] split = packetName.split(":");
                String dataOne = split[0];
                String addressStr = dataOne.substring(1);
                InetAddress address = InetAddress.getByName(addressStr);
                String port = split[1];
                DatagramPacket send = new DatagramPacket(byteArray, byteArray.length, address, Integer.parseInt(port));
                socket.send(send);
                System.out.println("已发送");
            }
        }
    }

}
