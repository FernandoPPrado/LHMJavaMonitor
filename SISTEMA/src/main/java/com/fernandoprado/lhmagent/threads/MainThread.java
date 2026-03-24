package com.fernandoprado.lhmagent.threads;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fernandoprado.lhmagent.client.LhmClient;
import com.fernandoprado.lhmagent.service.HardwareBusca;
import com.fernandoprado.lhmagent.service.HardwareFinder;
import feign.Feign;
import feign.jackson.JacksonDecoder;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainThread {

    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    HardwareBusca hardwareBusca = new HardwareBusca();
    HardwareFinder hardwareFinder = new HardwareFinder();
    GetThread getThread = new GetThread();

    public void start(JsonNode node) {

        Map<String, String> hashPath = hardwareBusca.encontrarPath(node);

        executorService.scheduleAtFixedRate(

                () -> {

                    if (isRunning.compareAndSet(false, true)) {
                        try {
                            getThread.getLHMInfo(hashPath, isRunning);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    } else {
                        System.out.println("Pulando... Leitura anterior ainda em curso.");
                    }


                }


                , 0, 250, TimeUnit.MILLISECONDS);

    }

    public void stop() {
        executorService.shutdown();
    }


}



