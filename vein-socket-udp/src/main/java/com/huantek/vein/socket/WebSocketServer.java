package com.huantek.vein.socket;


import com.alibaba.fastjson.JSONObject;
import com.corundumstudio.socketio.SocketIOServer;
import com.huantek.vein.Service.ServiceImpl.*;
import com.huantek.vein.util.BeforeCloseTreatment;
import com.huantek.vein.util.Ready;
import lombok.extern.slf4j.Slf4j;

import java.net.DatagramSocket;

@Slf4j
public class WebSocketServer {


    SocketIOServer socketIOServer;
    public static int socketPort;
    DatagramSocket socket;


    public WebSocketServer(DatagramSocket socket, SocketIOServer server) {
        this.socket = socket;
        this.socketIOServer = server;
    }

    public void socketStart() {

        //连接监听
        socketIOServer.addConnectListener(client -> {
            String clientInfo = client.getRemoteAddress().toString();
            String clientIp = clientInfo.substring(1, clientInfo.indexOf(":"));
            client.sendEvent("cliented", "IP:" + clientIp);
        });

        //断开连接监听
        socketIOServer.addDisconnectListener(client -> {
            String clientInfo = client.getRemoteAddress().toString();
            String clientIp = clientInfo.substring(1, clientInfo.indexOf(":"));
            client.sendEvent("disconnect", "IP:" + clientIp);
            //new BeforeCloseTreatment(serverSocket,socketMap).recycling();
        });

        socketIOServer.addEventListener("msgInfo", String.class, (client, data, ackRequest) -> {
            String clientInfo = client.getRemoteAddress().toString();
            String clientIp = clientInfo.substring(1, clientInfo.indexOf(":"));
            log.debug("WebSocket客户端" + clientIp + ": " + data);
            JSONObject jsonObject = JSONObject.parseObject(data);
            String control = jsonObject.getString("control");
            //String jsonMessage = null;
            //根据control参数进行判断调用方法
            if ("calibrate".equals(control)) new ActionCalibrate(socketIOServer).VeinCalibrate(jsonObject);//动作校准
            if ("firmwareVersionUp".equals(control)) new FVClass(socket).firmwareVersion(jsonObject);//固件升级
            if ("switch".equals(control)) new DMClass(socket).deviceManagement(jsonObject);//开关控制
            if ("firmwareSetting".equals(control))
                new FirmwareSetting(socket, socketIOServer).firmwareDetailSetting(jsonObject);
            if ("ledControl".equals(control)) new LEDControl(socket).ledService(jsonObject);//led控制
            if ("BVHControl".equals(control)) new BVHMClass(socketIOServer).writeBVH(jsonObject);//BVH录制
            if ("testControl".equals(control))
                new TestSendAndReceive(socket).testCommunicationService(jsonObject);//通信耗时测试
            if ("wifiSwitch".equals(control)) new WifiSwitch(socket).serviceWIFI(jsonObject);//切换WIFI
            if ("recordControl".equals(control)) new RecordControl().record(jsonObject);//录制
            if ("changeSize".equals(control)) new ChangeSize().changeModelSize(jsonObject);//改变模型大小
            if ("closeServer".equals(control)) new BeforeCloseTreatment().recycling();//关闭服务
            if ("streamingControl".equals(control)) new StreamingData(socketIOServer).streaming(jsonObject);//串流数据
            if ("modelControl".equals(control)) new ModelControl().modelCommand(jsonObject);//模型控制
            if ("dataQueueCount".equals(control)) new DataQueue().dataQueueCount();//当前数据缓存帧数
            if ("keyboard".equals(control)) new pressKeyboard().inputKeyCode(jsonObject);//键入键盘
            if ("handyMapping".equals(control)) new handyMapping().handyAssort(jsonObject);//handy配套
            if ("frameControl".equals(control)) new frameControl(socket).FPS(jsonObject);//FPS
            if ("pickUpCalibration".equals(control)) new pickUpControl(socket).pickUpCalibration(jsonObject);//传感器校准
            if ("addServer".equals(control)) new addServer().JmdNSService(jsonObject, socketPort);//选择网段注册后端服务
            if ("dataTest".equals(control)) new dataTest(socketIOServer).dataTestInit(jsonObject);//数据可视化输出文本
            if ("BVH2FBX".equals(control)) new BVH2FBX(socketIOServer).toFBX(jsonObject);//BVH2FBX
            if ("startSoftware".equals(control)) new startSoftware(socketIOServer).startup(jsonObject);
            if ("TESTDATA".equals(control)) new TestData().output();
            if ("isReady".equals(control)) new Ready(socketIOServer).yes(jsonObject);//告诉前端后端已经准备就绪
        });
    }


}