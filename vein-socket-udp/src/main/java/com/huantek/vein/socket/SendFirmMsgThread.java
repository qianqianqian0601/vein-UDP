package com.huantek.vein.socket;

import com.huantek.vein.Model.FirmNode;
import com.huantek.vein.Model.FirmSuit;
import com.huantek.vein.util.PublicVariable;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
public class SendFirmMsgThread {
    public void ListReferencesEvent() {
        try {
            Timer overTime = new Timer();
            overTime.schedule(new TimerTask() {
                @SneakyThrows
                @Override
                public void run() {
                    try {
                        for (FirmSuit firmSuit : PublicVariable.firmSuits) {
                            CopyOnWriteArrayList<FirmNode> firmNodes = firmSuit.getFirmNodes();
                            if (null != firmNodes) {
                                int count = 0;
                                FirmNode firmNode;
                                Iterator<FirmNode> iterator = firmNodes.iterator();
                                while (iterator.hasNext()) {
                                    if ((firmNode = iterator.next()) != null) {
                                        long timeMillis = System.currentTimeMillis();
                                        Long refreshTime = firmNode.getRefreshTime();
                                        if (timeMillis - refreshTime >= 3000) {
                                            String markKey = firmSuit.getProductID() + "_" + firmNodes.get(count).getMAC();
                                            String packetKey = SocketServer.markAndPacket.get(markKey);
                                            if (packetKey != null) {
                                                String[] split = packetKey.split(":");
                                                SocketServer.packetAndNode.remove(split[0]);
                                            }
                                            SocketServer.markAndPacket.remove(markKey);
                                            firmNodes.set(count, null);
                                            if (count <= 19) {
                                                for (int i = 0; i < PublicVariable.connectingList.size(); i++) {
                                                    if (PublicVariable.connectingList.get(i) == count + 1) {
                                                        PublicVariable.connectingList.remove(i);
                                                        log.debug("节点：" + (count + 1) + "超时3000ms被删除！！！！！！");
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    count++;
                                }
                            }
                            boolean nodesIsEmpty = nodesIsEmpty(firmSuit);//判断节点是否为NULL,true为NULL,false不为NULL
                            if (nodesIsEmpty) {//当节点信息为空的时候
                                PublicVariable.firmSuits.remove(firmSuit);//删除设备列表中相对的套装信息
                            }
                        }
                        if (PublicVariable.firmSuits.isEmpty()) {//当设备列表为null的时候
                            FirmSuit initSuit = new FirmSuit();
                            initSuit.setProductID("");
                            initSuit.setFirmNodes(null);
                            PublicVariable.firmSuits.add(initSuit);//初始化设备列表，使下次可以正常满足连接条件
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, 1000, 1000);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * //判断节点是否为NULL,true为NULL,false不为NULL
     *
     * @param suit
     * @return
     */
    private static boolean nodesIsEmpty(FirmSuit suit) {
        CopyOnWriteArrayList<FirmNode> firmNodes = suit.getFirmNodes();
        if (null == firmNodes) {
            return true;
        } else {
            for (FirmNode firmNode : firmNodes) {
                if (null != firmNode) return false;
            }
            return true;
        }
    }
}
