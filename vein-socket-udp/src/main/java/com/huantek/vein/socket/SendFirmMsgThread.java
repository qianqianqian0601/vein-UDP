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
                                                        log.debug("?????????" + (count + 1) + "??????3000ms???????????????????????????");
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    count++;
                                }
                            }
                            boolean nodesIsEmpty = nodesIsEmpty(firmSuit);//?????????????????????NULL,true???NULL,false??????NULL
                            if (nodesIsEmpty) {//??????????????????????????????
                                PublicVariable.firmSuits.remove(firmSuit);//??????????????????????????????????????????
                            }
                        }
                        if (PublicVariable.firmSuits.isEmpty()) {//??????????????????null?????????
                            FirmSuit initSuit = new FirmSuit();
                            initSuit.setProductID("");
                            initSuit.setFirmNodes(null);
                            PublicVariable.firmSuits.add(initSuit);//???????????????????????????????????????????????????????????????
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
     * //?????????????????????NULL,true???NULL,false??????NULL
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
