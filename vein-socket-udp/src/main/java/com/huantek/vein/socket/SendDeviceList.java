package com.huantek.vein.socket;

import com.alibaba.fastjson.JSONObject;
import com.corundumstudio.socketio.SocketIOServer;
import com.huantek.vein.util.PublicVariable;
import lombok.extern.slf4j.Slf4j;
import org.jctools.maps.NonBlockingHashMap;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static io.netty.util.concurrent.FastThreadLocal.size;

@Slf4j
public class SendDeviceList {

    private SocketIOServer finalServer;

    public SendDeviceList(SocketIOServer server) {
        this.finalServer = server;
    }

    public void sendDeviceListEvents() {
        JSONObject deviceList = new JSONObject();

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                deviceList.put("control", "deviceList");
                deviceList.put("data", PublicVariable.firmSuits);
                finalServer.getBroadcastOperations().sendEvent("msgInfo", deviceList);
            }
        }, 0, 3000);

//        new Timer().schedule(new TimerTask() {
//            @Override
//            public void run() {
//                log.debug(Arrays.toString(PublicVariable.testCount)+" 时间："+ LocalDateTime.now());
//                List<NonBlockingHashMap<Integer, double[]>> maps = PublicVariable.dataHashMapsORI;
//                for (int i = 0; i < maps.size(); i++) {
//                    NonBlockingHashMap map = maps.get(i);
//                    if (!map.isEmpty()){
//                        PublicVariable.testMapCount[i] = map.size();
//                    }
//                }
//                log.info(Arrays.toString(PublicVariable.testMapCount)+" 时间："+ LocalDateTime.now());
//                PublicVariable.testMapCount = new int[30];
//                PublicVariable.testCount = new int[29];
//            }
//        }, 0, 1000);
    }

}
