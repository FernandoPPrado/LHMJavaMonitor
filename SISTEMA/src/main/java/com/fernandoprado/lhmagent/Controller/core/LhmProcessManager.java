package com.fernandoprado.lhmagent.Controller.core;

import java.io.IOException;

public class LhmProcessManager {

    private Process process;

    public void startLhm() throws IOException {
        ProcessBuilder pb = new ProcessBuilder("LibreHardwareMonitor/LibreHardwareMonitor.exe");
        pb.start();
    }



}





