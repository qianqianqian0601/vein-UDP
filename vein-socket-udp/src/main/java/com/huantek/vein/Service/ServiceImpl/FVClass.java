package com.huantek.vein.Service.ServiceImpl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.huantek.vein.socket.SocketServer;
import com.huantek.vein.util.PublicVariable;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

@Slf4j
public class FVClass {

    DatagramSocket socket;

    public FVClass(DatagramSocket socket) {
        this.socket = socket;
    }

    //根据参数获取调用方法
    public void firmwareVersion(JSONObject jsonObject) {
        String action = jsonObject.getString("action");
        if (action != null || !"".equals(action)) {
            if ("singleUP".equals(action)) singleUP(jsonObject);//单个升级
            if ("allUP".equals(action)) allUP(jsonObject);//整套升级
        } else {
            log.debug("参数未传入");
        }
    }

    private void singleUP(JSONObject jsonObject) {//单个升级
        String suit = jsonObject.getString("productID");
        String mac = jsonObject.getString("MAC");
        String url = jsonObject.getString("URL");
        String socketName = suit + "_" + mac;
        sendFirVersionUPMSG(socketName, url);
    }

    private void allUP(JSONObject jsonObject) {//整套升级
        String suit = jsonObject.getString("productID");
        JSONArray macs = jsonObject.getJSONArray("MAC");
        String url = jsonObject.getString("URL");
        for (Object mac : macs) {
            String socketName = suit + "_" + mac;
            sendFirVersionUPMSG(socketName, url);
        }
    }

    //发送固件版本升级Url
    public void sendFirVersionUPMSG(String socketName, String url) {
        try {
            String DNS = url.substring(7, 47);//文件DNS
            int length = url.length();
            String path = url.substring(48, length);//文件储存路径
            path = "/" + path;
            byte[] dnsByte = ByteZeroFill(DNS.getBytes(), 60);//dns转Byte[]补零
            byte[] pathByte = ByteZeroFill(path.getBytes(), 64);//dns转Byte[]补零
            byte len = (byte) (dnsByte.length + pathByte.length);
            String packetName = SocketServer.markAndPacket.get(socketName);
            String[] split = packetName.split(":");
            String dataOne = split[0];//ip有斜杠
            String addressStr = dataOne.substring(1);
            InetAddress address = InetAddress.getByName(addressStr);
            String port = split[1];
            if (socket != null) {
                byte[] arr = {0x47, 0x05, len};//固件升级详情
                ByteArrayOutputStream stream = new ByteArrayOutputStream();//拼接Byte[]
                stream.write(arr, 0, arr.length);
                stream.write(dnsByte, 0, dnsByte.length);
                stream.write(pathByte, 0, pathByte.length);
                stream.write(0x74);
                byte[] VersionUp = stream.toByteArray();
                DatagramPacket send = new DatagramPacket(VersionUp, VersionUp.length, address, Integer.parseInt(port));
                socket.send(send);
                log.debug("正在进行软件升级----");
                for (int i = 0; i < VersionUp.length; i++) {
                    System.out.print(VersionUp[i] + " ");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
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
