package com.fernandoprado.lhmagent.Controller.core;

import java.io.IOException;

public class LhmProcessManager {

    public static Process process;

    public void startLhm() throws IOException {
        ProcessBuilder pb = new ProcessBuilder("LibreHardwareMonitor/LibreHardwareMonitor.exe");
        process = pb.start();
    }

    public static void closeLhm() {
        process.destroyForcibly();
    }


}





