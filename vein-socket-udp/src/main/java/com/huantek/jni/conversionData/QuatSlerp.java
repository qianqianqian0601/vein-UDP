package com.huantek.jni.conversionData;

import com.sun.jna.Library;
import com.sun.jna.Native;
import org.apache.ibatis.javassist.bytecode.stackmap.BasicBlock;

public class QuatSlerp {

    public interface QLibray extends Library {
            QLibray INSTANCE =
                    Native.loadLibrary(
                            "QuatSlerp",
                            QLibray.class);

            void slerp(double[] q1, double[] q2, double gamma, double[] output);

    }
}
