package com.huantek.vein.util;

import lombok.extern.slf4j.Slf4j;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
@Slf4j
public class SignInJmDNS {

    //注册的type是这个的话，你的发现设备的代码中的type也是这个才能找到这个设备。
    private final static String REMOTE_TYPE = "_http._tcp.local";

    private static JmDNS jmdns;

    private ServiceInfo mServiceInfo;

    public void signInServer(final String ip, int socketPort) throws InterruptedException {
        try {
            //服务的注册
            jmdns = JmDNS.create(InetAddress.getByName(ip), "RegisterService");
            PublicVariable.jmDNSHashMap.put(ip, jmdns);
            mServiceInfo = ServiceInfo.create(REMOTE_TYPE, "VeinMoCap", socketPort, "VeinMoCap服务");
            PublicVariable.serviceInfoHashMap.put(jmdns, mServiceInfo);
            jmdns.registerService(mServiceInfo);
            //服务的发现
            jmdns.addServiceListener(REMOTE_TYPE, new SearchJmDNS.SampleListener());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void unSignInServer(String ip) {
        try {
            jmdns = PublicVariable.jmDNSHashMap.get(ip);
            ServiceInfo serviceInfo = PublicVariable.serviceInfoHashMap.get(jmdns);
            if (jmdns!=null){
                jmdns.unregisterService(serviceInfo);
                log.debug("VeinMoCap服务已注销...");
            }else {
                log.debug("VeinMoCap服务未注册...");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
