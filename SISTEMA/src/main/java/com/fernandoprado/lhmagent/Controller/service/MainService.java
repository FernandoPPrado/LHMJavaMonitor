package com.fernandoprado.lhmagent.Controller.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fernandoprado.lhmagent.Controller.client.LhmClient;
import com.fernandoprado.lhmagent.Controller.core.LhmProcessManager;
import com.fernandoprado.lhmagent.Controller.enviroment.EnvConfiguration;
import com.fernandoprado.lhmagent.Controller.messaging.configuration.RabbitMQProvider;
import com.fernandoprado.lhmagent.Controller.messaging.service.MessagingService;
import com.fernandoprado.lhmagent.Controller.model.AppEvent;
import com.fernandoprado.lhmagent.Controller.threads.MainThread;
import com.fernandoprado.lhmagent.view.TrayView;
import com.fernandoprado.lhmagent.view.ViewPrint;
import feign.Feign;
import feign.jackson.JacksonDecoder;

import java.awt.*;
import java.io.IOException;
import java.util.concurrent.SubmissionPublisher;

public class MainService {
    private final SubmissionPublisher<AppEvent<?>> submissionPublisher = new SubmissionPublisher<>();
    ViewPrint viewPrint = new ViewPrint(submissionPublisher);
    TrayView trayView = new TrayView(submissionPublisher);
    HardwareFinder hardwareFinder = new HardwareFinder();
    MessagingService messagingService = new MessagingService(submissionPublisher);
    MainThread mainThread = new MainThread(submissionPublisher);

    public LhmClient client = Feign.builder().decoder(new JacksonDecoder()).target(LhmClient.class, EnvConfiguration.LHM_CLIENT_URL);


    public void initProgram() {

        try {

            LhmProcessManager lhmProcessManager = new LhmProcessManager();
            lhmProcessManager.startLhm();
            JsonNode node = null;
            int controller = 0;
            do {

                try {
                    node = client.getHardwareData();
                    Thread.sleep(2000);

                    if (controller >= 10) {
                        System.out.println("FALHA CONEXAO INICIAL");
                        RabbitMQProvider.closeConnection();
                        LhmProcessManager.closeLhm();
                        System.exit(1);
                    }
                    controller++;

                } catch (Exception e) {
                    e.printStackTrace();
                }

            } while (node == null);

            mainThread.start(node);
            trayView.createIcon();


        } catch (IOException | AWTException e) {
            throw new RuntimeException(e);
        }

    }

}
