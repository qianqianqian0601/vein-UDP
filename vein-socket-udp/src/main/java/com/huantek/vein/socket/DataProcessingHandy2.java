package com.huantek.vein.socket;

import com.huantek.vein.util.PublicVariable;
import com.huantek.vein.util.TransformUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.jctools.maps.NonBlockingHashMap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;

public class DataProcessingHandy2 extends Thread {

    private Integer number;
    private List<Byte> pfd;

    public DataProcessingHandy2(Integer sensorNumber, List<Byte> pfd) {
        this.number = sensorNumber;
        this.pfd = pfd;
    }

    @Override
    public void run() {
        if (number == 18 || number == 19) {
            List<NonBlockingHashMap<Integer, double[]>> dataHashMapsORI = PublicVariable.dataHashMapsORI;
            List<Byte> firstFinger, middleFinger, ringFinger, littleFinger, pollexTop, pollexEnd, hand , frameData;
            frameData = pfd.subList(3, 7);
            firstFinger = pfd.subList(7, 23);
            middleFinger = pfd.subList(23, 39);
            ringFinger = pfd.subList(39, 55);
            littleFinger = pfd.subList(55, 71);
            pollexTop = pfd.subList(71, 87);
            pollexEnd = pfd.subList(87, 103);
            hand = pfd.subList(103, 119);
            int frame = TransformUtil.bytesToIntsmall(TransformUtil.ListToByte(frameData), 0);
            double[] firstFingerJoinTemp = generate(firstFinger);

            double[] middleFingerJoinTemp = generate(middleFinger);

            double[] ringFingerJoinTemp = generate(ringFinger);

            double[] littleFingerJoinTemp = generate(littleFinger);

            double[] pollexTopJoinTemp = generate(pollexTop);

            double[] pollexEndJoinTemp = generate(pollexEnd);

            double[] handJoinTemp = generate(hand);

            if (number == 18) {
                dataHashMapsORI.get(number - 1).put(frame , firstFingerJoinTemp);

                dataHashMapsORI.get(number).put(frame , middleFingerJoinTemp);

                dataHashMapsORI.get(number + 1).put(frame , ringFingerJoinTemp);

                dataHashMapsORI.get(number + 2).put(frame , littleFingerJoinTemp);

                dataHashMapsORI.get(number + 4).put(frame , pollexTopJoinTemp);//

                dataHashMapsORI.get(number + 3).put(frame , pollexEndJoinTemp);//算法需要顺序是先指尾后指尖，所以顺序交换

                dataHashMapsORI.get(6).put(frame,handJoinTemp);//替换手部
            } else if (number == 19) {
                dataHashMapsORI.get(number + 4).put(frame , firstFingerJoinTemp);

                dataHashMapsORI.get(number + 5).put(frame , middleFingerJoinTemp);

                dataHashMapsORI.get(number + 6).put(frame , ringFingerJoinTemp);

                dataHashMapsORI.get(number + 7).put(frame , littleFingerJoinTemp);

                dataHashMapsORI.get(number + 9).put(frame , pollexTopJoinTemp);//

                dataHashMapsORI.get(number + 8).put(frame , pollexEndJoinTemp);//算法需要顺序是先指尾后指尖，所以顺序交换

                dataHashMapsORI.get(13).put(frame , handJoinTemp);//替换手部
            }
        }
    }


    /**
     * handy2数据没有加速度，所以需要拼接一个空的加速度
     * @param handy2Data
     * @return
     */
    public static double[] handy2DataJoinTemp(double[] handy2Data){
        double[] temp = {0 , 0 , 0};
        double[] bytes = ArrayUtils.addAll(handy2Data, temp);
        return bytes;
    }

    /**
     * 根据list生成
     *
     * @param list
     * @return
     */
    public static double[] generate(List<Byte> list) {
        double[] data =  {TransformUtil.byte2float(TransformUtil.ListToByte(list.subList(0, 4)), 0),
                TransformUtil.byte2float(TransformUtil.ListToByte(list.subList(4, 8)), 0),
                TransformUtil.byte2float(TransformUtil.ListToByte(list.subList(8, 12)), 0),
                TransformUtil.byte2float(TransformUtil.ListToByte(list.subList(12, 16)), 0)};
        double[] dataJoinTemp = handy2DataJoinTemp(data);
        return dataJoinTemp;
    }

}
