package com.fernandoprado.lhmagent.Controller.threads;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fernandoprado.lhmagent.Controller.model.AppEvent;
import com.fernandoprado.lhmagent.Controller.service.HardwareBusca;
import com.fernandoprado.lhmagent.Controller.service.HardwareFinder;
import com.fernandoprado.lhmagent.view.TrayView;
import com.fernandoprado.lhmagent.view.ViewPrint;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class MainThread {
    private final SubmissionPublisher<AppEvent<?>> submissionPublisher;
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    HardwareBusca hardwareBusca = new HardwareBusca();
    GetThread getThread;
    ;

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
                            e.printStackTrace();

                        }

                    } else {
                        System.out.println("Pulando... Leitura anterior ainda em curso.");
                    }


                }


                , 0, 1000, TimeUnit.MILLISECONDS);

    }

    public void stop() {
        executorService.shutdown();
    }


}



