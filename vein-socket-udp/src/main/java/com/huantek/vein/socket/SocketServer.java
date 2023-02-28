package com.huantek.vein.socket;

import com.alibaba.fastjson.JSONObject;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOServer;
import com.huantek.jni.conversionData.ConversionDataHandy2;
import com.huantek.jni.conversionData.KeyFrameInterpolation;
import com.huantek.vein.Model.FirmNode;
import com.huantek.vein.Model.FirmSuit;
import com.huantek.vein.Model.FrameCount;
import com.huantek.vein.util.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jctools.maps.NonBlockingHashMap;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component("SocketServer")
public class SocketServer implements ApplicationListener<ContextRefreshedEvent> {
    public static String dataTestFlag = "stop";
    public static long newConnectTime;
    public static boolean connectFlag = false;
    public static String dataTestPath;
    public static int dataTestNum = 0;
    private static SocketIOServer server;
    public static boolean masterSwitch = true;
    private static int websocketPort = 0;
    private static int socketPort = 0;
    public static int count = 0;
    public static String softwareFlag = "";
    public static NonBlockingHashMap<String, Integer> packetAndNode = new NonBlockingHashMap<>();//节点地址和端口做KEY 获取节点号
    public static NonBlockingHashMap<String, String> markAndPacket = new NonBlockingHashMap<>();//产品ID和MAC做KEY 获取节点地址和端口
    public static NonBlockingHashMap<String, String> packetAndMark = new NonBlockingHashMap<>();//节点地址和端口做KEY 获取产品ID和MAC
    public static NonBlockingHashMap<String, FirmNode> packetAndNodeModel = new NonBlockingHashMap<>();//产品ID和MAC做KEY 获取节点对象
    public static ExecutorService veinDataExecutor = Executors.newFixedThreadPool(10);
    static Vector<Byte> bytes = new Vector<>();
    static KeyFrameInterpolation keyFrameInterpolation = null;

    @SneakyThrows
    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        // TODO Auto-generated method stub
        socketSeverStart();
    }

    /**
     * 根据类型给可用的端口号
     *
     * @param testPort
     * @param array
     * @param type
     */
    public void givePort(TestPort testPort, int[] array, String type) {
        for (int port : array) {
            boolean flag = testPort.isLocalePortUsing(port);
            if (flag == false) {
                if (type.equals("wsp")) {
                    websocketPort = port;
                } else {
                    socketPort = port;
                    WebSocketServer.socketPort = socketPort;
                }
                break;
            }
        }
    }

    public void socketSeverStart() throws IOException {
        int[] websocketPortArray = {30000, 30001, 30002, 30003, 30004, 30005};
        TestPort testPort = new TestPort();
        givePort(testPort, websocketPortArray, "wsp");

        Configuration config = new Configuration();//new 一个配置项
        config.setHostname("127.0.0.1");//设置本机IP
        config.setPort(websocketPort);//设置监听端口
        config.setMaxFramePayloadLength(1024 * 1024);
        config.setMaxHttpContentLength(1024 * 1024);
        server = new SocketIOServer(config);//new websocketIO服务
        server.start();//启动websocketIO
        log.debug("WebSocket启动，等待PC连接...");

        int[] socketPortArray = {9999, 9977, 7777, 4444, 9944, 7744};
        givePort(testPort, socketPortArray, "sp");

        DatagramSocket socket = new DatagramSocket(socketPort);//创建UDP端口

        WebSocketServer webSocketServer2 = new WebSocketServer(socket, server);//websocket前端通信
        webSocketServer2.socketStart();


        File fileTest;
        FileWriter writerTest = null;
        log.debug("广播端口已就绪 广播端口为:" + socketPort);

        List<PriorityQueue> priorityQueues = PublicVariable.nodePriorityQueueList();
        try {
            int acceptCount = 0;//记录连接个数
            byte[] packetSize = new byte[1024];
            // 创建 packet接收 socket中的数据
            DatagramPacket packet = new DatagramPacket(packetSize, packetSize.length);
            while (masterSwitch) {
                socket.receive(packet); //接收客户端传来的数据 一直阻塞直到获取到一个数据报为止
                byte[] data = packet.getData();
                int packetLength = packet.getLength();
                InetAddress address = packet.getAddress();
                String nodeAddress = address.toString();
                int port = packet.getPort();
                String packetName = nodeAddress + ":" + port;

                if (data[0] == (byte) 70 && data[1] == (byte) 105) {//连接时的“firmware”标识
                    log.debug("节点地址:" + nodeAddress + "_端口号:" + port);
                    try {
                        DatagramPacket send = new DatagramPacket(OrderBase.QUERY_FIRMWARE_INFO, OrderBase.QUERY_FIRMWARE_INFO.length, address, port);
                        socket.send(send);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (!packetAndNode.containsKey(nodeAddress)) {
                        acceptCount++;
                        packetAndNode.put(nodeAddress, -1);
                        log.debug("连接数量：" + acceptCount);
                        if (acceptCount == 1) {
                            //RecordFrameRate recordFrameRate = new RecordFrameRate();//记录帧数
                            //recordFrameRate.start();
                            startSendData startSendData = new startSendData(socket);
                            startSendData.sendDataEvent();
                            SendDeviceList sendDeviceList = new SendDeviceList(server);//初始化发送设备列表方法
                            sendDeviceList.sendDeviceListEvents();
                            SendFirmMsgThread sendFirmMsgThread = new SendFirmMsgThread();//启动监听设备列表断开逻辑
                            sendFirmMsgThread.ListReferencesEvent();
                        }
                    } else {
                        packetAndNode.replace(nodeAddress, -1);
                    }

                } else {
                    if (packetAndNode.containsKey(nodeAddress)) {
//                        for (byte datum : data) {
//                            bytes.add(datum);
//                        }
                        for (int i = 0; i < packetLength; i++) {
                            bytes.add(data[i]);
                        }
                        List<Byte> pfd = parseFirmwareData();

                        if (pfd != null) {
                            Byte aByte = pfd.get(1);
                            //设备详情解析
                            //以及设备列表的处理
                            if (aByte == 0x04) {
                                String productID = String.valueOf(TransformUtil.bytesToIntsmall(TransformUtil.ListToByte(pfd.subList(3, 7)), 0));//产品ID
                                int sensorNumber = pfd.get(7);//传感器序号
                                log.info("节点号"+sensorNumber);
                                if (1 <= sensorNumber && sensorNumber <= 17) {
                                    PriorityQueue priorityQueue = priorityQueues.get(sensorNumber - 1);
                                    priorityQueue.clear();//传感器连接 帧数记录队列清空 重新计数
                                    log.debug("节点编号：" + sensorNumber + " 帧数记录队列Size：" + priorityQueue.size());
                                }
                                String mac = TransformUtil.bytesToHex(TransformUtil.ListToByte(pfd.subList(8, 14)));
                                float hardwareVersion = TransformUtil.byte2float(TransformUtil.ListToByte(pfd.subList(14, 18)), 0);//硬件版本
                                float firmWareVersion = TransformUtil.byte2float(TransformUtil.ListToByte(pfd.subList(18, 22)), 0);//固件版本
                                log.debug("固件版本:" + firmWareVersion);
                                int pickUpLevel = pfd.get(22);//校准等级
                                String SSID = new String(TransformUtil.ListToByte(pfd.subList(23, 55)), "UTF-8");//wifi名称
                                if (sensorNumber == 18) {
                                    productID = "handy(Left)";
                                    PublicVariable.connectingList.add(7);//取的相对应的map要—1所以add()的节点号都+1//标识连接的节点号
                                    PublicVariable.connectingList.add(18);
                                    PublicVariable.connectingList.add(19);
                                    PublicVariable.connectingList.add(20);
                                    PublicVariable.connectingList.add(21);
                                    PublicVariable.connectingList.add(22);
                                    PublicVariable.connectingList.add(23);
                                } else if (sensorNumber == 19) {
                                    productID = "handy(Right)";
                                    PublicVariable.connectingList.add(14);
                                    PublicVariable.connectingList.add(24);
                                    PublicVariable.connectingList.add(25);
                                    PublicVariable.connectingList.add(26);
                                    PublicVariable.connectingList.add(27);
                                    PublicVariable.connectingList.add(28);
                                    PublicVariable.connectingList.add(29);
                                }else {
                                    PublicVariable.connectingList.add(sensorNumber);
                                }
                                String nodeMark = productID + "_" + mac;
                                if (packetAndNode.containsKey(nodeAddress)) {//存入节点号
                                    packetAndNode.replace(nodeAddress, sensorNumber);
                                }
                                if (markAndPacket.containsKey(nodeMark)) {//存入地址和端口信息，设备控制用
                                    markAndPacket.replace(nodeMark, packetName);
                                } else {
                                    markAndPacket.put(nodeMark, packetName);
                                }
                                if (packetAndMark.containsKey(nodeAddress)) {//产品ID和MAC
                                    packetAndMark.replace(nodeAddress, nodeMark);
                                } else {
                                    packetAndMark.put(nodeAddress, nodeMark);
                                }
                                FirmNode firmNode = new FirmNode();
                                firmNode.setMAC(mac);//存入节点MAC
                                firmNode.setFirmVersion(firmWareVersion);//存入节点固件版本
                                firmNode.setHardwareVersion(hardwareVersion);//存入节点硬件版本
                                firmNode.setSensorNumber(sensorNumber);//存入节点号
                                firmNode.setPickUpLevel(pickUpLevel);//存入节点传感器校准等级
                                firmNode.setSSID(SSID);//存入节点连接WIFI名称
                                firmNode.setRefreshTime(System.currentTimeMillis());//存入接收到该节点信息的时间

                                if (packetAndNodeModel.containsKey(nodeAddress)) {//存入节点对象
                                    packetAndNodeModel.replace(nodeAddress, firmNode);
                                } else {
                                    packetAndNodeModel.put(nodeAddress, firmNode);
                                }

                                int subScript = NodeUtil.suitIsExist(productID);//判断设备列表中是否存在该产品ID的设备，true存在，false不存在
                                if (subScript == -1) {//当列表当中不存在该产品ID的设备时
                                    FirmSuit firmSuit = new FirmSuit();
                                    firmSuit.setProductID(productID);
                                    if (!PublicVariable.firmSuits.isEmpty() && PublicVariable.firmSuits.get(0).getProductID().equals("")) {//判断设备列表是不是初始化的，是初始化的覆盖在0下标上
                                        PublicVariable.firmSuits.set(0, firmSuit);
                                    } else if (!PublicVariable.firmSuits.isEmpty() && !PublicVariable.firmSuits.get(0).getProductID().equals("")) {//不是初始化的在列表数组后追加
                                        PublicVariable.firmSuits.add(firmSuit);
                                    }
                                } else {
                                    FirmSuit Suit = PublicVariable.firmSuits.get(subScript);
                                    CopyOnWriteArrayList<FirmNode> firmNodes = Suit.getFirmNodes();
                                    if (sensorNumber > 0) {
                                        firmNodes.set(sensorNumber - 1, firmNode);
                                    } else if (sensorNumber == 0) {
                                        int i = NodeUtil.nodeIsExist(firmNodes, mac);
                                        if (i == -1) {
                                            Suit.getFirmNodes().add(firmNode);
                                        } else {
                                            Suit.getFirmNodes().set(i, firmNode);
                                        }
                                    }
                                }

                                if (newConnectTime != 0) {
                                    long timeMillis = System.currentTimeMillis();//当前时间
                                    long l = timeMillis - newConnectTime;//与上一连接相差时间
                                    if (l > 500 && !SocketServer.markAndPacket.isEmpty()) {//连接超过500ms并且地址不为空的发送停止命令
                                        int count = 0;
                                        long num = SocketServer.packetAndNode.values().stream().filter(a -> a != -1).count();
                                        if (num == SocketServer.markAndPacket.size()) {
                                            for (String ipAndPort : SocketServer.markAndPacket.values()) {
                                                String[] split = ipAndPort.split(":");
                                                String dataOne = split[0];
                                                String addressStr = dataOne.substring(1);
                                                InetAddress sendAddress = InetAddress.getByName(addressStr);
                                                String sendPort = split[1];
                                                count++;
                                                log.debug(count + "::::" + socket.getPort());
                                                //发送停止指令
                                                DatagramPacket send = new DatagramPacket(OrderBase.STOP_MOTION_CAPTURE, OrderBase.STOP_MOTION_CAPTURE.length, sendAddress, Integer.parseInt(sendPort));
                                                socket.send(send);
                                            }
                                            log.debug("所有节点停止发送数据---");
                                        }
                                    }
                                }
                                newConnectTime = System.currentTimeMillis();//记录新的连接时间

                            }

                            if (packetAndNode.get(nodeAddress) == -1) {//没有查询到节点 继续查询
                                //查询设备详情指令
                                socket.send(new DatagramPacket(OrderBase.QUERY_FIRMWARE_INFO, OrderBase.QUERY_FIRMWARE_INFO.length, packet.getAddress(), packet.getPort()));
                                log.debug("重查");
                                continue;
                            }

                            if (dataTestFlag.equals("start")) {
                                dataTestNum++;
                                if (dataTestNum == 1) {
                                    log.debug("开始记录+++++");
                                    fileTest = new File(dataTestPath);
                                    if (!fileTest.exists()) {
                                        writerTest = new FileWriter(fileTest);
                                        fileTest.createNewFile();
                                    } else {
                                        writerTest = new FileWriter(fileTest, true);
                                    }
                                }
                                Integer sensorNumber = packetAndNode.get(nodeAddress);
                                StringBuffer sbf = new StringBuffer();
                                sbf.append("{").append(sensorNumber).append(":").append(System.currentTimeMillis()).append("}");
                                String toString = sbf.toString();
                                writerTest.write(toString);
                            } else if (dataTestFlag.equals("readyStop")) {
                                writerTest.flush();
                                writerTest.close();
                                dataTestNum = 0;
                                dataTestFlag = "stop";
                                log.debug("结束记录+++++");
                            }

                            //电量信息查询
                            if (aByte == 0x03) {
                                int cellPrice = pfd.get(3) & 0xff;
                                int cellEvent = pfd.get(4) & 0xff;
                                FirmNode firmNode = packetAndNodeModel.get(nodeAddress);
                                firmNode.setCellPrice(cellPrice);//存入电量信息
                                firmNode.setCellEvent(cellEvent);//存入电量事件
                            }

                            //数据开始和发送指令的回执
                            if (aByte == 0x14) {
                                int status = pfd.get(3);
                                long count = packetAndNode.values().stream().filter(a -> a != -1).count();
                                if (status == 1) {
                                    if (PublicVariable.meteringStart == 1) {
                                        PublicVariable.startTimes[0] = System.currentTimeMillis();
                                        log.info("开始第一个： " + PublicVariable.startTimes[0]);
                                    }
                                    if (PublicVariable.meteringStart >= count) {
                                        PublicVariable.startTimes[1] = System.currentTimeMillis();
                                        log.info("开始最后一个： " + PublicVariable.startTimes[1]);
                                        log.info("开始相差： " + (PublicVariable.startTimes[1] - PublicVariable.startTimes[0]));
                                        PublicVariable.meteringStart = 0;
                                        if (null != keyFrameInterpolation) {
                                            keyFrameInterpolation.insertFramesCancel();
                                        }
                                        keyFrameInterpolation = new KeyFrameInterpolation();
                                        keyFrameInterpolation.insertFrameEvent();
                                    }
                                    PublicVariable.meteringStart++;
                                } else if (status == 0) {
                                    if (PublicVariable.meteringEnd == 1) {
                                        PublicVariable.endTimes[0] = System.currentTimeMillis();
                                        log.info("停止第一个： " + PublicVariable.endTimes[0]);
                                    }
                                    if (PublicVariable.meteringEnd >= count) {
                                        connectFlag = false;
                                        PublicVariable.endTimes[1] = System.currentTimeMillis();
                                        log.info("停止最后一个： " + PublicVariable.endTimes[1]);
                                        log.info("停止相差： " + (PublicVariable.endTimes[1] - PublicVariable.endTimes[0]));
                                        PublicVariable.meteringEnd = 0;
                                    }
                                    PublicVariable.meteringEnd++;
                                }
                            }

                            //handy2校准命令
                            if (aByte == 0x13) {
                                PublicVariable.CbFlagHandy2 = false;
                                int sensorNumber = pfd.get(3);
                                log.info("节点"+sensorNumber);
                                int type = pfd.get(4);
                                log.info("type"+type);
                                if (type == 1) {//校准
                                    log.info("handy校准");
                                    if (sensorNumber == 18) ConversionDataHandy2.pose = 0;
                                    if (sensorNumber == 19) ConversionDataHandy2.pose = 1;
                                }
                            }

                            //传感器校准等级
                            if (aByte == 0x12) {
                                FirmNode firmNode = packetAndNodeModel.get(nodeAddress);
                                JSONObject pickUpStatus = new JSONObject();
                                if (pfd.get(3) == 0x01) pickUpStatus.put("pickUpStatus", "PickUpCalibration-Start");
                                if (pfd.get(3) == 0x02) {
                                    pickUpStatus.put("pickUpStatus", "PickUpCalibration-End");
                                    if (pfd.get(4) == 0x01) firmNode.setPickUpLevel(1);
                                    if (pfd.get(4) == 0x02) firmNode.setPickUpLevel(2);
                                    if (pfd.get(4) == 0x03) firmNode.setPickUpLevel(3);
                                }
                                if (pfd.get(3) == 0x03) {
                                    if (pfd.get(4) == 0x01)
                                        pickUpStatus.put("pickUpStatus", "PickUpCalibration-Succeed");
                                    if (pfd.get(4) == 0xe1) pickUpStatus.put("pickUpStatus", "PickUpCalibration-Fail");
                                    if (pfd.get(4) == 0xe2)
                                        pickUpStatus.put("pickUpStatus", "PickUpCalibration-OverTime");
                                }
                                server.getBroadcastOperations().sendEvent("msgInfo", pickUpStatus);
                            }

                            //信号强度查询
                            if (aByte == 0x0A) {
                                FirmNode FirmNode = packetAndNodeModel.get(nodeAddress);
                                Integer SensorNumber = packetAndNode.get(nodeAddress);
                                String Mark = packetAndMark.get(nodeAddress);//获取产品id_MAC
                                int Signal = pfd.get(3);
                                InetAddress sendAddress = packet.getAddress();
                                int sendPort = packet.getPort();
                                //新建传感器信号设备列表处理线程
                                SignalThread signalThread = new SignalThread(FirmNode, SensorNumber, Signal, socket, sendAddress, sendPort, Mark);
                                veinDataExecutor.execute(signalThread);
                            }

                            //固件升级返回信息
                            if (aByte == 0x05) {
                                JSONObject updateStatus = new JSONObject();
                                Byte status = pfd.get(3);
                                if (status == 0x01) {
                                    updateStatus.put("updateStatus", 0);
                                }
                                if (status == 0) {
                                    updateStatus.put("updateStatus", 1);
                                }
                                updateStatus.put("control", "FirmVersionUp");
                                server.getBroadcastOperations().sendEvent("msgInfo", updateStatus);//发送给前端
                            }

                            //节点配置返回信息
                            if (aByte == 0x08) {
                                JSONObject nodeSetUP = new JSONObject();
                                Byte status = pfd.get(3);
                                if (status == 0x01) {
                                    nodeSetUP.put("updateStatus", 0);
                                }
                                if (status == 0) {
                                    nodeSetUP.put("updateStatus", 1);
                                }
                                nodeSetUP.put("control", "firmwareSetting");
                                nodeSetUP.put("action", "nodeSetUP");
                                server.getBroadcastOperations().sendEvent("msgInfo", nodeSetUP);//发送给前端
                            }

                            //产品ID配置返回信息
                            if (aByte == 0x09) {
                                JSONObject productIDSetUP = new JSONObject();
                                Byte status = pfd.get(3);
                                if (status == 0x01) {
                                    productIDSetUP.put("updateStatus", 0);
                                }
                                if (status == 0) {
                                    productIDSetUP.put("updateStatus", 1);
                                }
                                productIDSetUP.put("control", "firmwareSetting");
                                productIDSetUP.put("action", "productIDSetUP");
                                server.getBroadcastOperations().sendEvent("msgInfo", productIDSetUP);//发送给前端
                            }

                            // handy2校准 1
                            if (pfd.get(1)==0x13 && SocketServer.softwareFlag.equals("Handy2")){
                                PublicVariable.CbFlagHandy2 = false;
                                int sensorNumberHandy = pfd.get(3);
                                int type = pfd.get(4);
                                if (type==2){//校准
                                    if (sensorNumberHandy == 18) ConversionDataHandy2.handy2Pose = 0;
                                    if (sensorNumberHandy == 19) ConversionDataHandy2.handy2Pose = 1;
                                }
                            }

                            //FPS设置回复信息
                            if (aByte == 0x15) {
                                Byte one = pfd.get(3);
                                Byte two = pfd.get(4);
                                Byte three = pfd.get(5);
                                Byte four = pfd.get(6);
                                if (one == 0xff) {
                                    log.warn("设置帧率失败");
                                } else {
                                    byte[] array = {one, two, three, four};
                                    int time = TransformUtil.bytesToIntsmall(array, 0);
                                    log.info("设置成功 每帧时间：" + time);
                                }
                            }

                            //vein动作数据的转换
                            if (aByte == 0x00 && packetAndNode.get(nodeAddress) != -1) {
                                String mark = packetAndMark.get(nodeAddress);
                                //Integer sensorNumber = packetAndNode.get(nodeAddress);
                                List<Byte> frameDate = pfd.subList(3, 7);
                                int frame = TransformUtil.bytesToIntsmall(TransformUtil.ListToByte(frameDate), 0);
                                int sensorNumber = pfd.get(39);//传感器序号
                                if (SocketServer.dataTestFlag.equals("start")) {
                                    if (SocketServer.dataTestNum == 1) {
                                        log.debug("丢失帧计数开始！！！！");
                                    }
                                    FrameCount frameCount = new FrameCount();
                                    frameCount.setFrame(frame);
                                    frameCount.setTimeMillis(System.currentTimeMillis());
                                    PriorityQueue priorityQueue = priorityQueues.get(sensorNumber - 1);
                                    priorityQueue.offer(frameCount);
                                    if (priorityQueue.size() > 10) {
                                        FrameCount poll = (FrameCount) priorityQueue.poll();//获取首位元素并删除
                                        FrameCount peek = (FrameCount) priorityQueue.peek();//获取首位元素不删除
                                        Integer frontFrame = poll.getFrame();

                                        Integer backFrame = peek.getFrame();

                                        if (backFrame - frontFrame > 1) {
                                            File file = new File(SocketServer.dataTestPath + ".lackCount.txt");
                                            FileWriter fileWriter;
                                            if (!file.exists()) {
                                                fileWriter = new FileWriter(file);
                                                file.createNewFile();
                                            } else {
                                                fileWriter = new FileWriter(file, true);
                                            }
                                            int missCount = backFrame - frontFrame - 1;//相差帧数
                                            int nodeMissCount = PublicVariable.frameLackCount[sensorNumber - 1];//获取此节点累积丢帧计数
                                            PublicVariable.frameLackCount[sensorNumber - 1] = nodeMissCount + missCount;//修改节点丢帧累积数
                                            StringBuffer sbf = new StringBuffer();
                                            sbf.append("node:").append(sensorNumber).append(" missCount:").append(missCount)
                                                    .append(" nodeMissSumCount:").append(PublicVariable.frameLackCount[sensorNumber - 1]).append(" frontFrame:")
                                                    .append(frontFrame).append(" backFrame:").append(backFrame)
                                                    .append(" time:").append(peek.getTimeMillis()).append("\r\n");
                                            fileWriter.write(sbf.toString());
                                            fileWriter.flush();
                                            fileWriter.close();
                                        }
                                    }
                                } else if (SocketServer.dataTestFlag.equals("readyStop")) {
                                    log.debug("丢失帧计数结束！！！！");
                                }

                                //记录第一次存入队列时间，为缓存400ms做准备
                                if (PublicVariable.queueCacheTimeFlag) {
                                    PublicVariable.queueCacheTime = System.currentTimeMillis();
                                    PublicVariable.queueCacheTimeFlag = false;
                                }

                                //VEIN数据处理线程
                                DataProcessingVein dataProcessingVein = new DataProcessingVein(mark, sensorNumber, pfd);
                                veinDataExecutor.execute(dataProcessingVein);
                            }

                            //handy2数据的转换
                            if (aByte == 0x01 && packetAndNode.get(nodeAddress) != -1) {
                                Integer sensorNumber = packetAndNode.get(nodeAddress);
                                DataProcessingHandy2 dataProcessingHandy2 = new DataProcessingHandy2(sensorNumber, pfd);
                                veinDataExecutor.execute(dataProcessingHandy2);
                            }
                        }
                    }
                }
            }
            server.stop();
            socket.close();
            BeforeCloseTreatment.dosClose();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * 读取固件数据报文
     *
     * @return
     */
    public List<Byte> parseFirmwareData() {
        for (int i = 0; i < bytes.size(); i++) {//循环Vector
            if (bytes.get(i) == 0x47) {//首先找到帧头
                if (bytes.size() < i + 3) {//避免在获取数据长度时数组越界
                    return null;
                }
                int len = (bytes.get(i + 2) & 0xff) + 3;//获取报文长度
                if (bytes.size() - 1 - i >= len) {//长度足够
                    if (bytes.get(i + len) == 0x74) {//找帧尾
                        List<Byte> sub;
                        Vector<Byte> clone = (Vector<Byte>) bytes.clone();
                        sub = clone.subList(i, i + len + 1);//将获取的完整报文放在新的容器中
                        bytes.subList(0, i + len + 1).clear();//将已经获取到的报文删除，继续获取下一个报文
                        return sub;
                    } else {//继续循环找帧尾
//                        Vector<Byte> errorData = (Vector<Byte>) bytes.clone();
//                        List<Byte> errorDataList = errorData.subList(i, i + len + 1);
//                        LostFrame.errorDataLogFirm(errorDataList);
                        bytes.subList(0, i + len + 1).clear();
                        continue;
                    }
                } else {//长度不够继续向Vector存数据
                    return null;
                }
            } else {//继续循环找帧头
                continue;
            }
        }
        return null;
    }

}
