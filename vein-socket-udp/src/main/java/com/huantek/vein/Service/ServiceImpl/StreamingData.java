package com.huantek.vein.Service.ServiceImpl;

import com.alibaba.fastjson.JSONObject;
import com.corundumstudio.socketio.SocketIOServer;
import com.huantek.vein.socket.SendStreamingMsg;
import com.huantek.vein.util.PublicVariable;
import com.huantek.vein.util.TestPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class StreamingData {

    static ServerSocket streamingSocket;
    static DatagramSocket streamingDatagramSocket;
    private SocketIOServer socketIOServer;
    private final String control = "streaming";

    public StreamingData(SocketIOServer socketIOServer) {
        this.socketIOServer = socketIOServer;
    }

    public void streaming(JSONObject jsonObject) throws IOException {
        String action = jsonObject.getString("action");
        Integer type = jsonObject.getInteger("type");
        if (action.equals("startStreaming")) {
            Integer streamingPort = jsonObject.getInteger("streamingPort");
            String order = jsonObject.getString("order");
            PublicVariable.threadFlagTwo = true;
            if (type == 0) {
                int streamingSocket = createStreamingSocket(streamingPort, type);
                if (1 != streamingSocket) {
                    return;
                }
                ExecutorService executorService = Executors.newFixedThreadPool(1);
                executorService.submit(() -> sendStreamingData(order));
            } else if (type == 1) {
                int streamingSocket = createStreamingSocket(streamingPort, type);
                if (1 != streamingSocket) {
                    return;
                }
                ExecutorService executorService = Executors.newFixedThreadPool(1);
                executorService.submit(() -> sendStreamingDataUDP(order, type, streamingPort));
            }


        } else if (action.equals("endStreaming")) {
            PublicVariable.threadFlagTwo = false;
            try {
                if (!streamingSocket.isClosed()) streamingSocket.close();
                log.debug("close success!");
            } catch (Exception e) {
                e.printStackTrace();
                log.debug("close failed!");
            }
        }
    }


    /**
     * 启动并创建串流socket
     *
     * @param port 端口号
     * @param type
     * @return
     */
    private int createStreamingSocket(int port, Integer type) {
        TestPort testPort = new TestPort();
        boolean flag = testPort.isLocalePortUsing(port);
        boolean flag2 = testPort.isLocalePortUsing(port - 1);

        JSONObject streamingException = new JSONObject();
        if (flag == true || flag2 == true) {
            log.debug("The streaming port is occupied!");
            streamingException.put("control", control);
            streamingException.put("statusCode", "305");
            streamingException.put("message", "The streaming port is occupied!");
            socketIOServer.getBroadcastOperations().sendEvent("msgInfo", streamingException);
            return 1;
        } else {
            try {
                if (type == 0) {
                    streamingSocket = new ServerSocket(port);
                    log.debug("TCP Streaming started successfully!");
                    streamingException.put("message", "TCP Streaming started successfully!");
                } else if (type == 1) {
                    streamingDatagramSocket = new DatagramSocket(port - 1);
                    log.debug("UDP Streaming started successfully!");
                    streamingException.put("message", "UDP Streaming started successfully!");
                }
                streamingException.put("control", control);
                streamingException.put("statusCode", "0");
                socketIOServer.getBroadcastOperations().sendEvent("msgInfo", streamingException);
                return 0;
            } catch (Exception e) {
                streamingException.put("control", control);
                streamingException.put("statusCode", "304");
                streamingException.put("message", "Streaming startup failed!");
                socketIOServer.getBroadcastOperations().sendEvent("msgInfo", streamingException);
                log.debug("Streaming startup failed!");
                e.printStackTrace();
                return 1;
            }
        }
    }


    /**
     * 发送串流数据
     *
     * @param order 转轴顺序
     */
    @Async
    void sendStreamingData(String order) {
        try {
            SendStreamingMsg sendStreamingMsg = new SendStreamingMsg(streamingSocket, order);
            sendStreamingMsg.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 发送串流数据
     *
     * @param order         转轴顺序
     * @param streamingPort
     */
    @Async
    void sendStreamingDataUDP(String order, Integer type, Integer streamingPort) {
        try {
            SendStreamingMsg sendStreamingMsg = new SendStreamingMsg(streamingDatagramSocket, order, type, streamingPort);
            sendStreamingMsg.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
