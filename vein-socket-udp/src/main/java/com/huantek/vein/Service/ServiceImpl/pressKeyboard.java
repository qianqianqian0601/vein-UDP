package com.huantek.vein.Service.ServiceImpl;

import com.alibaba.fastjson.JSONObject;
import com.huantek.jni.conversionData.ConversionData;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class pressKeyboard {
    public void inputKeyCode(JSONObject jsonObject) {
        Integer keyCode = jsonObject.getInteger("keyCode");
        if (keyCode != null) {
            ConversionData.tmp.pressKey(keyCode);
        } else {
            log.debug("KeyCode Error!");
        }
    }
}
