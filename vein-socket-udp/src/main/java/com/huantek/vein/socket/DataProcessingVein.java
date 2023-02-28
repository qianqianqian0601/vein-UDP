package com.huantek.vein.socket;

import com.huantek.vein.Model.FirmNode;
import com.huantek.vein.Model.FirmSuit;
import com.huantek.vein.util.NodeUtil;
import com.huantek.vein.util.PublicVariable;
import com.huantek.vein.util.TransformUtil;
import lombok.extern.slf4j.Slf4j;
import org.jctools.maps.NonBlockingHashMap;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static javax.swing.UIManager.put;

@Slf4j
public class DataProcessingVein extends Thread {
    private String mark;
    private Integer sensorNumber;
    private List<Byte> pfd;

    public DataProcessingVein(String mark, Integer sensorNumber, List<Byte> pfd) {
        this.mark = mark;
        this.sensorNumber = sensorNumber;
        this.pfd = pfd;
    }

    @Override
    public void run() {
//        String name = Thread.currentThread().getName();
//        System.out.println(name);
        int nodeIndex = 0;
        String[] split = mark.split("_");
        String productID = split[0];
        String mac = split[1];
        for (FirmSuit firmSuit : PublicVariable.firmSuits) {
            if (firmSuit.getProductID().equals(productID)) {//判断产品ID获取同一套设备节点信息
                CopyOnWriteArrayList<FirmNode> firmNodes = firmSuit.getFirmNodes();
                //通过mac判断设备列表中同一套装下是否有该节点设备存在
                //如果存在则返回该节点在设备列表中的下标，不存在返回-1
                nodeIndex = NodeUtil.nodeIsExist(firmNodes, mac);
            }
        }

        //四元素单数 加速度复数
        List<Byte> W = null, X = null, Y = null, Z = null, XX = null, YY = null, ZZ = null, frameDate = null;
        frameDate = pfd.subList(3, 7);
        W = pfd.subList(7, 11);
        X = pfd.subList(11, 15);
        Y = pfd.subList(15, 19);
        Z = pfd.subList(19, 23);
        XX = pfd.subList(23, 27);
        YY = pfd.subList(27, 31);
        ZZ = pfd.subList(31, 35);
        float WData = TransformUtil.byte2float(TransformUtil.ListToByte(W), 0);
        float XData = TransformUtil.byte2float(TransformUtil.ListToByte(X), 0);
        float YData = TransformUtil.byte2float(TransformUtil.ListToByte(Y), 0);
        float ZData = TransformUtil.byte2float(TransformUtil.ListToByte(Z), 0);
        float XXData = TransformUtil.byte2float(TransformUtil.ListToByte(XX), 0);
        float YYData = TransformUtil.byte2float(TransformUtil.ListToByte(YY), 0);
        float ZZData = TransformUtil.byte2float(TransformUtil.ListToByte(ZZ), 0);
        int frame = TransformUtil.bytesToIntsmall(TransformUtil.ListToByte(frameDate), 0);

        if (((WData * WData + XData * XData + YData * YData + ZData * ZData) - 1) <= 0.0001) {
            if (sensorNumber != null && !sensorNumber.equals(0) && nodeIndex >= 0 && nodeIndex < 17) {
                double[] oriAndAcc = {WData, XData, YData, ZData,XXData,YYData,ZZData};
                NonBlockingHashMap<Integer, double[]> map = PublicVariable.dataHashMapsORI.get(sensorNumber - 1);
                if (map.containsKey(frame)){
                    map.replace(frame,oriAndAcc);
                }else {
                    map.put(frame,oriAndAcc);
                }
                PublicVariable.testCount[sensorNumber-1]++;
//                double[] ac = {XXData, YYData, ZZData};
//                PublicVariable.dataHashMapsACC.get(sensorNumber - 1).put(frame,ac);
            }
        } else {
            if (SocketServer.dataTestFlag.equals("start")) {
                if (SocketServer.dataTestNum == 1) {
                    log.debug("~~~~不规范帧丢弃计数开始~~~~");
                }
                try {
                    File file = new File(SocketServer.dataTestPath + ".errorDataCount.txt");
                    FileWriter fileWriter;
                    if (!file.exists()) {
                        fileWriter = new FileWriter(file);
                        file.createNewFile();
                    } else {
                        fileWriter = new FileWriter(file, true);
                    }
                    PublicVariable.giveUpCount[sensorNumber - 1]++;
                    int giveUpCount = PublicVariable.giveUpCount[sensorNumber - 1];
                    StringBuffer sbf = new StringBuffer();
                    sbf.append("node:").append(sensorNumber).append(" giveUpCount:").append(giveUpCount)
                            .append(" giveUpFrame:").append(frame).append(" time:")
                            .append(System.currentTimeMillis()).append("\r\n");
                    fileWriter.write(sbf.toString());
                    fileWriter.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (SocketServer.dataTestFlag.equals("readyStop")) {
                log.debug("~~~~不规范帧丢弃计数结束~~~~");
            }
        }
    }
}
