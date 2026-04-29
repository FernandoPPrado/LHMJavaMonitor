package com.fernandoprado.lhmagent.Controller.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fernandoprado.lhmagent.Controller.client.LhmClient;
import com.fernandoprado.lhmagent.Controller.core.LhmProcessManager;
import com.fernandoprado.lhmagent.Controller.enviroment.EnvConfiguration;
import com.fernandoprado.lhmagent.Controller.logger.model.LogData;
import com.fernandoprado.lhmagent.Controller.logger.service.LogService;
import com.fernandoprado.lhmagent.Controller.messaging.configuration.RabbitMQProvider;
import com.fernandoprado.lhmagent.Controller.messaging.service.MessagingService;
import com.fernandoprado.lhmagent.Controller.model.AppEvent;
import com.fernandoprado.lhmagent.Controller.threads.MainThread;
import com.fernandoprado.lhmagent.view.TrayView;
import com.fernandoprado.lhmagent.view.ViewPrint;
import feign.Feign;
import feign.jackson.JacksonDecoder;

import java.io.IOException;
import java.util.concurrent.SubmissionPublisher;

public class MainService {
    private final SubmissionPublisher<AppEvent<?>> submissionPublisher = new SubmissionPublisher<>();
    ViewPrint viewPrint = new ViewPrint(submissionPublisher);
    TrayView trayView = new TrayView(submissionPublisher);
    HardwareFinder hardwareFinder = new HardwareFinder();
    MessagingService messagingService = new MessagingService(submissionPublisher);
    MainThread mainThread = new MainThread(submissionPublisher);
    LogService logService = new LogService(submissionPublisher);

    public LhmClient client = Feign.builder().decoder(new JacksonDecoder()).target(LhmClient.class, EnvConfiguration.LHM_CLIENT_URL);


    public void initProgram() throws IOException {

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
                        submissionPublisher.submit(new AppEvent<LogData>(AppEvent.EventType.LOG_ERROR, new LogData(MainService.class.getSimpleName(), "ERRO AO CONECTAR AO LHM", null)));
                        RabbitMQProvider.closeConnection();
                        LhmProcessManager.closeLhm();
                        System.exit(1);
                    }
                    controller++;

                } catch (Exception e) {
                    submissionPublisher.submit(new AppEvent<LogData>(AppEvent.EventType.LOG_ERROR, new LogData(MainService.class.getSimpleName(), "ERRO AO INICIAR O PROGRAMA", e)));
                }

            } while (node == null);

            mainThread.start(node);


        } catch (IOException e) {
            submissionPublisher.submit(new AppEvent<LogData>(AppEvent.EventType.ERROR, new LogData(MainService.class.getSimpleName(), "ERRO AO CRIAR O ICONE", e)));
        }

    }

}
