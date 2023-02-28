package com.huantek.vein.Service.ServiceImpl;

import com.huantek.vein.socket.QueueDataToList;
import com.huantek.vein.util.PublicVariable;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class TestData {

    public void output() {
        QueueDataToList classA = new QueueDataToList();
        List<Integer> list = classA.removeDuplicate(PublicVariable.connectingList);
        for (Integer index : list) {
            int size = PublicVariable.dataHashMapsORI.get(index - 1).size();
            log.debug("元素个数：" + size + " 节点号：" + index + "time：" + System.currentTimeMillis());
        }
    }
}
