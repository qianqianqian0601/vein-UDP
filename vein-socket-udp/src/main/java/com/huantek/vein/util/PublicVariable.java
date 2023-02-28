package com.huantek.vein.util;

import com.alibaba.fastjson.JSONObject;
import com.huantek.vein.Model.FirmSuit;
import com.huantek.vein.Model.FrameAndAcc;
import com.huantek.vein.Model.FrameAndOri;
import com.huantek.vein.Model.FrameCount;
import org.jctools.maps.NonBlockingHashMap;
import org.jctools.queues.SpscUnboundedArrayQueue;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import java.net.DatagramSocket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class PublicVariable {
    public static ConcurrentHashMap<String, DatagramSocket> socketMap = new ConcurrentHashMap<>();//公共的socket集合
    public static HashMap<Integer, Long> timeMap = new HashMap<>();//udp socket 超时计时
    public static CopyOnWriteArrayList<FirmSuit> firmSuits = IntegratedData.initFirmSuitList();//初始化设备列表数组

    public static boolean veins = false;//程序是否启动标识
    public static boolean handy2 = false;
    public static boolean CbFlagHandy2 = true;//校准标志位handy2

    public static boolean threadFlagTwo = false;//串流标志位
    public static boolean U3DThread = false;//U3D标志位
    public static String recordThread = "stop";//日志标志位
    public static String calibrationFlag = "";
    public static long queueCacheTime = 0;//记录队列数据缓存时间
    public static boolean queueCacheTimeFlag = false;//记录队列数据缓存时间标志位
    public static boolean queueCallFlag = false;//队列取值标志位
    /**
     * 四元素公共变量
     *
     * @return
     */
    public static double[][] ori() {
        return oriOrOriHandy2(29, 4);
    }

    /**
     * 四元素公共变量handy2
     *
     * @return
     */
    public static double[][] oriHandy2() {
        return oriOrOriHandy2(14, 4);
    }

    public static double[][] oriOrOriHandy2(int one, int two) {
        double[][] ori = new double[one][two];
        for (int i = 0; i < ori.length; i++) {
            ori[i][0] = 1.00;
            ori[i][1] = 0;
            ori[i][2] = 0;
            ori[i][3] = 0;
        }
        return ori;
    }

    public static int meteringStart = 1;
    public static int meteringEnd = 1;
    public static long[] startTimes = {0, 0};
    public static long[] endTimes = {0, 0};
    public static long[] startToStop = {0, 0};

    public static double[][] ori = ori();
    public static double[][] oriHandy2 = oriHandy2();

    public static double[][] acc = new double[29][3]; //加速度公共变量

    public static JSONObject VeinData = new JSONObject();//动作数据

    public static int recordFrameCount = 0;

    public static List<List<Long>> sendTimeByNode = initSendTimeList();

    public static ArrayList<List<Long>> initSendTimeList() {
        List<Long> sendTime = new ArrayList<>();//发送固件信息时间集合
        ArrayList<List<Long>> lists = new ArrayList<>();
        for (int i = 0; i <= 17; i++) {
            lists.add(sendTime);
        }
        return lists;
    }

    public static List<Long> sendTimeALG = new ArrayList<>();//发送算法信息时间集合

    public static long[] beforeFrameORISORACCS = {
            0, 0, 0, 0, 0,
            0, 0, 0, 0, 0,
            0, 0, 0, 0, 0,
            0, 0
    };

    public static int[] testCount = {
            0, 0, 0, 0, 0,
            0, 0, 0, 0, 0,
            0, 0, 0, 0, 0,
            0, 0, 0, 0, 0,
            0, 0, 0, 0, 0,
            0, 0, 0, 0
    };
    public static int[] testMapCount = {
            0, 0, 0, 0, 0,
            0, 0, 0, 0, 0,
            0, 0, 0, 0, 0,
            0, 0, 0, 0, 0,
            0, 0, 0, 0, 0,
            0, 0, 0, 0, 0
    };

    public static int[] pubFrame = {
            0, 0, 0, 0, 0,
            0, 0, 0, 0, 0,
            0, 0, 0, 0, 0,
            0, 0
    };

    /**
     * ORI帧数记录
     */
    public static long[] beforeFrameORIS = beforeFrameORISORACCS;

    /**
     * ACC帧数记录
     */
    public static long[] beforeFrameACCS = beforeFrameORISORACCS;

    public static int[] VeinDataCountACCS = pubFrame;

    public static int[] VeinDataCountORIS = pubFrame;

    public static int[] frameOrder = pubFrame;

    public static int[] frameLackCount = pubFrame;

    public static int[] giveUpCount = pubFrame;

    public static List<Queue> QueuesOri = initQueuesOri();
    public static List<Queue> QueuesAcc = initQueuesAcc();
    public static List<NonBlockingHashMap<Integer, double[]>> dataHashMapsORI = queueListORI();

    /**
     * 四元素优先级队列
     *
     * @return
     */
    public synchronized static List<Queue> initQueuesOri() {
        List<Queue> PQueueList = new ArrayList<>();
        Queue<FrameAndOri> node = new SpscUnboundedArrayQueue<>(25);
        return autoList(PQueueList, node, 29);
    }

    /**
     * 加速度优先级队列
     *
     * @return
     */
    public synchronized static List<Queue> initQueuesAcc() {
        List<Queue> PQueueList = new ArrayList<>();
        Queue<FrameAndAcc> node = new SpscUnboundedArrayQueue<>(25);
        return autoList(PQueueList, node, 18);
    }

    /**
     * 构造一个17个队列的四元素数组
     *
     * @return
     */
    public static List<NonBlockingHashMap<Integer, double[]>> queueListORI() {
        return autoList(29);
    }


    //自定义对比器 对比帧号 帧号升序
    static Comparator<FrameCount> cFrameCount = Comparator.comparingInt(FrameCount::getFrame);

    public static List<PriorityQueue> nodePriorityQueueList() {
        List<PriorityQueue> PQueueList = new ArrayList<>();
        for (int i = 0; i < 17; i++) {
            PriorityQueue<FrameCount> node = new PriorityQueue<>(cFrameCount);
            PQueueList.add(node);
        }
        return PQueueList;
    }

    private static List<Queue> autoList(List<Queue> pQueueList, Queue node, int size) {
        for (int i = 0; i < size; i++) {
            pQueueList.add(node);
        }
        return pQueueList;
    }

    private static List<NonBlockingHashMap<Integer, double[]>> autoList(int size) {
        List<NonBlockingHashMap<Integer, double[]>> hashMaps = new ArrayList();
        for (int i = 0; i < size; i++) {
            NonBlockingHashMap<Integer, double[]> node = new NonBlockingHashMap<>();
            hashMaps.add(i, node);
        }
        return hashMaps;
    }

    //记录连接节点
    public static List<Integer> connectingList = new ArrayList<>();

    //服务注册和注销使用
    public static HashMap<String, JmDNS> jmDNSHashMap = new HashMap<>();
    public static HashMap<JmDNS, ServiceInfo> serviceInfoHashMap = new HashMap<>();
}
