package com.huantek.vein.Service.ServiceImpl;

import com.huantek.vein.util.PublicVariable;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Queue;

@Slf4j
public class DataQueue {

    private List<Queue> queuesACC = PublicVariable.QueuesAcc;

    public void dataQueueCount() {
        int maxLength = 0;
        for (int i = 0; i < queuesACC.size(); i++) {
            if (queuesACC.get(i).size() > maxLength) {
                maxLength = queuesACC.get(i).size();
            }
        }
        log.debug("现缓存最大帧数：" + maxLength);
        int minLength = maxLength;
        for (int i = 0; i < queuesACC.size(); i++) {
            if (queuesACC.get(i).size() < minLength) {
                minLength = queuesACC.get(i).size();
            }
        }
        log.debug("现缓存最小帧数：" + minLength);
    }
}
