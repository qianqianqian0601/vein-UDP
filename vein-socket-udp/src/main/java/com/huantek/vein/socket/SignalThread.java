package com.huantek.vein.socket;

import com.huantek.vein.Model.FirmNode;
import com.huantek.vein.Model.FirmSuit;
import com.huantek.vein.util.IntegratedData;
import com.huantek.vein.util.NodeUtil;
import com.huantek.vein.util.OrderBase;
import com.huantek.vein.util.PublicVariable;
import lombok.SneakyThrows;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class SignalThread extends Thread {

    private FirmNode firmNode;
    private Integer sensorNumber;
    private int signal;
    private DatagramSocket socket;
    private InetAddress address;
    private int port;
    private String mark;

    public SignalThread(FirmNode firmNode, Integer sensorNumber, int signal, DatagramSocket socket, InetAddress address,
                        int port,
                        String mark) {
        this.firmNode = firmNode;
        this.sensorNumber = sensorNumber;
        this.signal = signal;
        this.socket = socket;
        this.address = address;
        this.port = port;
        this.mark = mark;
    }

    @SneakyThrows
    @Override
    public void run() {
        firmNode.setSignal(signal);//存入对象属性
        firmNode.setRefreshTime(System.currentTimeMillis());//刷新节点接收到心跳的时间
        try {
            socket.send(new DatagramPacket(OrderBase.HEART_BEAT, OrderBase.HEART_BEAT.length, address, port));//回复心跳
        } catch (Exception e) {
            e.printStackTrace();
        }
        String[] split = mark.split("_");//使用_分割
        String productID = split[0];//产品ID
        String mac = split[1];//mac
        for (FirmSuit suit : PublicVariable.firmSuits) {//遍历设备列表获取单独的套装设备
            if (suit.getProductID().equals("") && suit.getFirmNodes() == null) {
                FirmSuit firmSuit = new FirmSuit();
                firmSuit.setProductID(productID);
                PublicVariable.firmSuits.set(0, firmSuit);
            }
            if (suit.getProductID().equals(productID)) {//判断产品ID获取同一套设备节点信息
                CopyOnWriteArrayList<FirmNode> firmNodes = suit.getFirmNodes();
                //通过mac判断设备列表中同一套装下是否有该节点设备存在
                //如果存在则返回该节点在设备 列表中的下标，不存在返回-1
                int num = NodeUtil.nodeIsExist(firmNodes, mac);
                if (num > -1 && num < 19) {//大于-1的话列表中存在该节点信息,小于17证明该节点有对应部位
                    if (sensorNumber > 0) {//节点信息大于0的时候
                        if (firmNodes.get(sensorNumber - 1) == null) {//该下标目前为空就放在改下标处
                            firmNodes.set(num, null);
                            firmNodes.set(sensorNumber - 1, firmNode);
                        } else if (firmNodes.get(sensorNumber - 1) != null && firmNodes.get(sensorNumber - 1).getMAC().equals(mac)) {
                            firmNodes.set(sensorNumber - 1, firmNode);
                        } else if (firmNodes.get(sensorNumber - 1) != null && !firmNodes.get(sensorNumber - 1).getMAC().equals(mac)) {
                            firmNodes.set(num, null);
                            firmNodes.add(firmNode);
                        }
                    } else if (sensorNumber == 0) {//等于0为临时节点
                        firmNodes.set(num, null);
                        firmNodes.add(firmNode);//临时节点追加在后面
                    }
                } else if (num >= 19) {//大于等于17证明该节点之前是临时节点
                    if (sensorNumber > 0) {//节点改变成大于0的数,将之前临时下标的节点信息删除，并将该节点信息放在对应下标上
                        if (firmNodes.get(sensorNumber - 1) == null) {
                            firmNodes.remove(num);
                            firmNodes.set(sensorNumber - 1, firmNode);
                        } else if (firmNodes.get(sensorNumber - 1) != null && !firmNodes.get(sensorNumber - 1).getMAC().equals(mac)) {
                            continue;
                        }
                    } else if (sensorNumber == 0) {
                        firmNodes.set(num, firmNode);
                    }
                } else if (num == -1) {//等于-1列表不存在该节点信息
                    if (sensorNumber > 0) {//节点号不为0,则放在对应的下标上
                        if (firmNodes.get(sensorNumber - 1) == null) {
                            firmNodes.set(sensorNumber - 1, firmNode);
                        } else if (firmNodes.get(sensorNumber - 1) != null && !firmNodes.get(sensorNumber - 1).getMAC().equals(mac)) {
                            firmNodes.add(firmNode);
                        }
                    } else if (sensorNumber == 0) {//节点号为0,则向后追加临时节点
                        firmNodes.add(firmNode);
                    }
                }
            }
        }

        boolean isEmpty = NodeUtil.nodesIsEmpty(PublicVariable.firmSuits.get(0));
//        if (isEmpty){
//            PublicVariable.firmSuits.remove(0);
//        }
//        if (PublicVariable.firmSuits.isEmpty()){
//            PublicVariable.firmSuits = IntegratedData.initFirmSuitList();
//        }
        if (isEmpty) {
            PublicVariable.firmSuits = IntegratedData.initFirmSuitList();
        }
    }
}
