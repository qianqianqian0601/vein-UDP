package com.huantek.vein.util;

import com.huantek.vein.socket.SocketServer;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Lazy(false)
public class BeforeCloseTreatment {

    public void recycling() {
        SocketServer.masterSwitch = false;
    }

    public static void dosClose() throws IOException {
        String cmd = "taskkill /f /im MotionHub-Server.exe";
        Runtime.getRuntime().exec(cmd);
    }


}
