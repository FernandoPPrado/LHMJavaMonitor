package com.fernandoprado.lhmagent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fernandoprado.lhmagent.Controller.client.LhmClient;
import com.fernandoprado.lhmagent.Controller.core.LhmProcessManager;
import com.fernandoprado.lhmagent.Controller.messaging.service.MessagingService;
import com.fernandoprado.lhmagent.Controller.model.AppEvent;
import com.fernandoprado.lhmagent.Controller.service.HardwareFinder;
import com.fernandoprado.lhmagent.Controller.service.MainService;
import com.fernandoprado.lhmagent.Controller.threads.MainThread;
import com.fernandoprado.lhmagent.view.TrayView;
import com.fernandoprado.lhmagent.view.ViewPrint;
import feign.Feign;
import feign.jackson.JacksonDecoder;

import java.awt.*;
import java.io.IOException;
import java.util.concurrent.SubmissionPublisher;

public class Main {

    static MainService mainService = new MainService();

    public static void main(String[] args) throws IOException {
            mainService.initProgram();
    }


}
