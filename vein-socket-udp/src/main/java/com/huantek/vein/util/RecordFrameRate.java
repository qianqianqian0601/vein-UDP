package com.huantek.vein.util;

import com.huantek.vein.socket.SendVeinData;
import com.huantek.vein.socket.SocketServer;
import com.huantek.vein.socket.SocketThreadAlg;
import lombok.SneakyThrows;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

public class RecordFrameRate extends Thread {

    @Override
    public void run() {
        new Timer().schedule(new TimerTask() {

            @Override
            public void run() {
                try {
                    File file = new File("./frameRateRecordLog" + LocalDate.now() + ".txt");
                    FileWriter writer;
                    if (!file.exists()) {
                        writer = new FileWriter(file);
                        file.createNewFile();
                    } else {
                        writer = new FileWriter(file, true);
                    }
                    Calendar calendar = Calendar.getInstance();
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String format = formatter.format(calendar.getTime());
                    writer.write(format + " ACC_COUNT:");
                    writer.write(format + " ORI_COUNT:");
                    writer.write(format + " sendFrameCountALG:" + SendVeinData.sendFrameCountALG + "\r\n");
                    writer.write(format + " receiveFrameCountALG:" + SocketThreadAlg.receiveFrameCountALG + "\r\n");
                    writer.write("\r\n");

                    writer.flush();
                    writer.close();
                    SendVeinData.sendFrameCountALG = 0;
                    SocketThreadAlg.receiveFrameCountALG = 0;
                    //SendVeinData.sendFrameCountALG = 0;
                    //SocketThreadAlg.receiveFrameCountALG = 0;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 5000, 1000);

    }

}
