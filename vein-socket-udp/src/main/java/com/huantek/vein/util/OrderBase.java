package com.huantek.vein.util;

public class OrderBase {

    //心跳
    public static final byte[] HEART_BEAT = {0x47, 0x0D, 0x00, 0x74};//查询信号强度报文

    //重置wifi
    public static final byte[] CLEAR_WIFI = {0x47, 0x0E, 0x00, 0x74};//重置WIFI命令

    //测试用
    public static final byte[] SEND_FIRMWARE_TEST_MSG = {0x47, 0x0B, 0x00, 0x74};//固件通信耗时测试命令

    //查询用
    public static final byte[] QUERY_FIRMWARE_INFO = {0x47, 0x04, 0x00, 0x74};//查询设备详情报文
    public static final byte[] QUERY_FIRMWARE_BATTERY = {0x47, 0x03, 0x02, 0x00, 0x00, 0x74};//查询电量信息报文

    //开关机
    public static final byte[] OFF_COM = {0x47, 0x06, 0x01, 0x01, 0x74};//关机指令
    public static final byte[] REBOOT_COM = {0x47, 0x06, 0x01, 0x02, 0x74};//重启指令

    //LED
    public static final byte[] LED_OFF = {0x47, 0x02, 0x01, 0x00, 0x74};//LED关闭指令
    public static final byte[] LED_OPEN = {0x47, 0x02, 0x01, 0x01, 0x74};//LED打开指令
    public static final byte[] LED_FLASH = {0x47, 0x02, 0x01, 0x02, 0x74};//LED闪烁指令

    //开始核心功能
    public static final byte[] START_MOTION_CAPTURE = {0x47, 0x0C, 0x01, 0x01, 0x74};//开始发送动捕数据
    public static final byte[] STOP_MOTION_CAPTURE = {0x47, 0x0C, 0x01, 0x00, 0x74};//停止发送动捕数据

    //fps
    public static final byte[] FPS_50 = {0x47, 0x10, 0x01, 0x00, 0x74};//FPS50设置
    public static final byte[] FPS_100 = {0x47, 0x10, 0x01, 0x01, 0x74};//FPS100设置
    public static final byte[] FPS_QUERY = {0x47, 0x10, 0x01, 0x02, 0x74};//FPS查询

    //传感器校准
    public static final byte[] PICKUP_CALIBRATION = {0x47, 0x12, 0x01, 0x01, 0x74};//磁力校准
}
