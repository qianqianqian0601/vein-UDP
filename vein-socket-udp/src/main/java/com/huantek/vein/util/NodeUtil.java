package com.huantek.vein.util;

import com.huantek.vein.Model.FirmNode;
import com.huantek.vein.Model.FirmSuit;

import java.util.concurrent.CopyOnWriteArrayList;

public class NodeUtil {
    /**
     * //判断设备列表中是否存在该产品ID的设备，-1不存在，存在返回对象下标
     *
     * @param productID
     * @return
     */
    public static int suitIsExist(String productID) {
        for (int i = 0; i < PublicVariable.firmSuits.size(); i++) {
            if (productID.equals(PublicVariable.firmSuits.get(i).getProductID())) {
                return i;
            }
        }
        return -1;
    }

    /**
     * //通过mac判断设备列表中同一套装下是否有该节点设备存在,如果存在则返回该节点在设备列表中的下标，不存在返回0
     *
     * @param firmNodes
     * @param Mac
     * @return
     */
    public static int nodeIsExist(CopyOnWriteArrayList<FirmNode> firmNodes, String Mac) {
        for (int i = 0; i < firmNodes.size(); i++) {
            if (firmNodes.get(i) != null && firmNodes.get(i).getMAC().equals(Mac)) return i;
        }
        return -1;
    }


    /**
     * //判断节点是否为NULL,true为NULL,false不为NULL
     *
     * @param suit
     * @return
     */
    public static boolean nodesIsEmpty(FirmSuit suit) {
        if (null == suit.getFirmNodes()) {
            return true;
        } else {
            CopyOnWriteArrayList<FirmNode> firmNodes = suit.getFirmNodes();
            for (FirmNode firmNode : firmNodes) {
                if (null != firmNode) return false;
            }
            return true;
        }
    }
}
