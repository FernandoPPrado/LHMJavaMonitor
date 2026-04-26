package com.fernandoprado.lhmagent.Controller.threads;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fernandoprado.lhmagent.Controller.client.LhmClient;
import com.fernandoprado.lhmagent.Controller.logger.model.LogData;
import com.fernandoprado.lhmagent.Controller.model.AppEvent;
import com.fernandoprado.lhmagent.Controller.service.HardwareFinder;
import feign.Feign;
import feign.jackson.JacksonDecoder;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class GetThread {

    AtomicReference<AppEvent<String>> oldAppEvent = new AtomicReference<>(new AppEvent<String>(AppEvent.EventType.INIT, ""));
    AtomicReference<AppEvent<String>> lastAppEvent = new AtomicReference<>(new AppEvent<String>(AppEvent.EventType.INIT, ""));


    public static ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    public static HardwareFinder hardwareFinder = new HardwareFinder();
    public static LhmClient client = Feign.builder()
            .decoder(new JacksonDecoder())
            .target(LhmClient.class, "http://localhost:8085");


    final SubmissionPublisher<AppEvent<?>> submissionPublisher;

    public GetThread(SubmissionPublisher<AppEvent<?>> sub) {
        this.submissionPublisher = sub;
    }

    public void getLHMInfo(Map<String, JsonPointer> mapaPath, AtomicBoolean atomicBoolean) {


        executor.submit(() -> {
            AppEvent<Map<String, String>> appEvent;
            try {
                JsonNode jsonNode = client.getHardwareData();
                Map<String, String> mapRetorno = (hardwareFinder.lerValoresAtuais(jsonNode, mapaPath));

                appEvent = new AppEvent<>(AppEvent.EventType.UPDATE, mapRetorno);
                submissionPublisher.submit(appEvent);
                lastAppEvent.set(new AppEvent<String>(AppEvent.EventType.OK, "OK"));

            } catch (Exception e) {
                submissionPublisher.submit(new AppEvent<LogData>(AppEvent.EventType.LOG_ERROR, new LogData(GetThread.class.toString(), "ERRO AO LER O JSON", e)));
                lastAppEvent.set(new AppEvent<String>(AppEvent.EventType.ERROR, "ERRO"));
            } finally {
                if (oldAppEvent.get().eventType() != lastAppEvent.get().eventType()) {
                    submissionPublisher.submit(lastAppEvent.get());
                }
                oldAppEvent.set(lastAppEvent.get());
                atomicBoolean.set(false);
            }


        });


    }

}
