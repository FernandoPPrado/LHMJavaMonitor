package com.fernandoprado.lhmagent.core;

import com.fernandoprado.lhmagent.threads.MainThread;
import com.sun.tools.javac.Main;

import java.io.IOException;

public class LhmProcessManager {

    private Process process;

    public void startLhm() throws IOException {
        ProcessBuilder pb = new ProcessBuilder("LibreHardwareMonitor/LibreHardwareMonitor.exe");
        pb.start();
    }


    public static void main(String[] args) throws IOException {


    }

}





