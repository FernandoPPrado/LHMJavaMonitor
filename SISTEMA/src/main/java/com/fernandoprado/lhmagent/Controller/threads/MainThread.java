package com.fernandoprado.lhmagent.Controller.threads;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fernandoprado.lhmagent.Controller.logger.model.LogData;
import com.fernandoprado.lhmagent.Controller.model.AppEvent;
import com.fernandoprado.lhmagent.Controller.service.HardwareBusca;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;


public class MainThread {
    private final SubmissionPublisher<AppEvent<?>> submissionPublisher;
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    HardwareBusca hardwareBusca = new HardwareBusca();
    GetThread getThread;

    public MainThread(SubmissionPublisher<AppEvent<?>> subs) {
        this.submissionPublisher = subs;
        getThread = new GetThread(submissionPublisher);
    }


    public void start(JsonNode node) {

        Map<String, JsonPointer> hashPath = hardwareBusca.encontrarPath(node);


        executorService.scheduleAtFixedRate(

                () -> {

                    if (isRunning.compareAndSet(false, true)) {
                        try {
                            getThread.getLHMInfo(hashPath, isRunning);
                        } catch (Exception e) {
                            submissionPublisher
                                    .submit(new AppEvent<>(AppEvent.EventType.LOG_ERROR, new LogData(MainThread.class.getSimpleName(), "FALHA AO CHAMAR O METODO GETTHREAD", e)));


                        }

                    } else {
                        submissionPublisher.submit(new AppEvent<>(AppEvent.EventType.LOG_WARN, new LogData(MainThread.class.getSimpleName(), "PULANDO LEITURA...", null)));
                    }


                }


                , 0, 1000, TimeUnit.MILLISECONDS);

    }

    public void stop() {
        executorService.shutdown();

    }


}



