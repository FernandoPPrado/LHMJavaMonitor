package com.fernandoprado.lhmagent.threads;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fernandoprado.lhmagent.client.LhmClient;
import com.fernandoprado.lhmagent.service.HardwareFinder;
import feign.Feign;
import feign.jackson.JacksonDecoder;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class GetThread {


    public static LhmClient client = Feign.builder()
            .decoder(new JacksonDecoder())
            .target(LhmClient.class, "http://localhost:8085");

    public static ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    public static HardwareFinder hardwareFinder = new HardwareFinder();

    public void getLHMInfo(Map<String, JsonPointer> mapaPath, AtomicBoolean atomicBoolean) {


        executor.submit(() -> {

            try {

                JsonNode jsonNode = client.getHardwareData();
                System.out.println(hardwareFinder.lerValoresAtuais(jsonNode, mapaPath));

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                atomicBoolean.set(false);
            }


        });


    }

}
