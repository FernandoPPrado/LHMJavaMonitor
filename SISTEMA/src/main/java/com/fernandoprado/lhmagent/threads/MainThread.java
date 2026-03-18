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

public class MainThread {

    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    int i = 0;

    LhmClient client = Feign.builder()
            .decoder(new JacksonDecoder())
            .target(LhmClient.class, "http://localhost:8085");


    HardwareBusca hardwareBusca = new HardwareBusca();
    HardwareFinder hardwareFinder = new HardwareFinder();

    public void start() {
        JsonNode node = null;

        Map<String, String> hashPath = hardwareBusca.encontrarPath(node);

        executorService.scheduleAtFixedRate(

                () -> {
                    try {

                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                }


                , 0, 250, TimeUnit.MILLISECONDS);

    }

    public void stop() {
        executorService.shutdown();
    }


}



