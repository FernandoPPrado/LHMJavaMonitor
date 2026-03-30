package com.fernandoprado.lhmagent.threads;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fernandoprado.lhmagent.client.LhmClient;
import com.fernandoprado.lhmagent.service.HardwareFinder;
import feign.Feign;
import feign.jackson.JacksonDecoder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.atomic.AtomicBoolean;

public class GetThread {
    final SubmissionPublisher submissionPublisher;
    Map<String, String> listaRetorno;
    ArrayList<Map<String, String>> list = new ArrayList<>();
    public static LhmClient client = Feign.builder()
            .decoder(new JacksonDecoder())
            .target(LhmClient.class, "http://localhost:8085");

    public static ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    public static HardwareFinder hardwareFinder = new HardwareFinder();

    public GetThread(SubmissionPublisher<ArrayList<Map<String, String>>> sub) {
        this.submissionPublisher = sub;
    }

    public void getLHMInfo(Map<String, JsonPointer> mapaPath, AtomicBoolean atomicBoolean) {


        executor.submit(() -> {

            try {
                JsonNode jsonNode = client.getHardwareData();
                listaRetorno = (hardwareFinder.lerValoresAtuais(jsonNode, mapaPath));

                if (list.size() == 1) {
                    submissionPublisher.submit(list);
                    list = new ArrayList<>();
                }
                list.add(listaRetorno);

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                atomicBoolean.set(false);
            }


        });


    }

}
