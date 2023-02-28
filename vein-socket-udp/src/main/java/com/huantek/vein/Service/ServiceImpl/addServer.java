package com.huantek.vein.Service.ServiceImpl;

import com.alibaba.fastjson.JSONObject;
import com.huantek.vein.util.SignInJmDNS;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class addServer {

    public void JmdNSService(JSONObject jsonObject, int socketPort) {
        String action = jsonObject.getString("action");
        if (action.equals("addService")) serviceRegistration(jsonObject, socketPort);
        if (action.equals("removeService")) serviceUnRegistration(jsonObject);
    }

    public void serviceRegistration(JSONObject jsonObject, int socketPort) {
        try {
            String IP = jsonObject.getString("address");
            SignInJmDNS signInJmDNS = new SignInJmDNS();
            signInJmDNS.signInServer(IP, socketPort);//注册VeinMoCap服务让固件可以找到
            log.debug("VeinMoCap服务注册完成，等待固件搜索连接...");
        } catch (Exception e) {
            e.printStackTrace();
            log.debug("服务注册异常");
        }
    }

    public void serviceUnRegistration(JSONObject jsonObject) {
        try {
            String IP = jsonObject.getString("address");
            SignInJmDNS signInJmDNS = new SignInJmDNS();
            signInJmDNS.unSignInServer(IP);//根据IP注销服务
        } catch (Exception e) {
            e.printStackTrace();
            log.debug("服务注销异常");
        }
    }
}
