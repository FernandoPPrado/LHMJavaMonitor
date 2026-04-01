package com.fernandoprado.lhmagent.Controller.threads;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fernandoprado.lhmagent.Controller.client.LhmClient;
import com.fernandoprado.lhmagent.Controller.model.AppEvent;
import com.fernandoprado.lhmagent.Controller.service.HardwareFinder;
import feign.Feign;
import feign.jackson.JacksonDecoder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.atomic.AtomicBoolean;

public class GetThread {

    public static ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    public static HardwareFinder hardwareFinder = new HardwareFinder();
    public static LhmClient client = Feign.builder()
            .decoder(new JacksonDecoder())
            .target(LhmClient.class, "http://localhost:8085");


    final SubmissionPublisher submissionPublisher;

    public GetThread(SubmissionPublisher<AppEvent<Map<String, String>>> sub) {
        this.submissionPublisher = sub;
    }

    public void getLHMInfo(Map<String, JsonPointer> mapaPath, AtomicBoolean atomicBoolean) {


        executor.submit(() -> {

            try {
                JsonNode jsonNode = client.getHardwareData();


                Map<String, String> mapRetorno = (hardwareFinder.lerValoresAtuais(jsonNode, mapaPath));

                AppEvent<Map<String, String>> appEvent = new AppEvent<>(AppEvent.EventType.UPDATE, mapRetorno);
                submissionPublisher.submit(appEvent);

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                atomicBoolean.set(false);
            }


        });


    }

}
