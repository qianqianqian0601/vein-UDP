package com.huantek.vein.Service.ServiceImpl;

import com.alibaba.fastjson.JSONObject;
import com.corundumstudio.socketio.SocketIOServer;
import com.huantek.vein.socket.SocketServer;
import com.huantek.vein.util.PublicVariable;

import java.util.List;
import java.util.PriorityQueue;

public class dataTest {
    SocketIOServer server;

    public dataTest(SocketIOServer socketIOServer) {
        this.server = socketIOServer;
    }

    public void dataTestInit(JSONObject jsonObject) {
        String action = jsonObject.getString("action");
        if (action.equals("start")) startTest(jsonObject);
        if (action.equals("stop")) stopTest();
    }

    public void startTest(JSONObject jsonObject) {
        String path = jsonObject.getString("path");
        SocketServer.dataTestPath = path;
        SocketServer.dataTestFlag = "start";
        JSONObject json = new JSONObject();
        jsonObject.put("control", "dataTest");
        jsonObject.put("message", "start");
        server.getBroadcastOperations().sendEvent("msgInfo", json);
    }

    public void stopTest() {
        SocketServer.dataTestFlag = "readyStop";
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("control", "dataTest");
        jsonObject.put("message", "stop");
        List<PriorityQueue> priorityQueues = PublicVariable.nodePriorityQueueList();
        for (PriorityQueue priorityQueue : priorityQueues) {
            priorityQueue.clear();
        }
        server.getBroadcastOperations().sendEvent("msgInfo", jsonObject);
    }
}
