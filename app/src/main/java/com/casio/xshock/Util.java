package com.casio.xshock;

import java.io.DataOutputStream;
import java.io.IOException;

class Util {

    public static void inputKeyevent(int keyevent) throws IOException {
        Process suProcess = Runtime.getRuntime().exec("su");
        DataOutputStream os = new DataOutputStream(suProcess.getOutputStream());
        os.writeBytes("adb shell" + "\n");
        os.flush();
        os.writeBytes("input keyevent " + keyevent + "\n");
        os.flush();
    }
}
