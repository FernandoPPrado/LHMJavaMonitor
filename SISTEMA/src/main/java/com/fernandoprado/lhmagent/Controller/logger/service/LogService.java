package com.fernandoprado.lhmagent.Controller.logger.service;

import com.fernandoprado.lhmagent.Controller.logger.model.LogData;
import com.fernandoprado.lhmagent.Controller.model.AppEvent;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.SubmissionPublisher;

public class LogService {

    public LogService(SubmissionPublisher<AppEvent<?>> submissionPublisher) {
        submissionPublisher.consume(this::logFilter);
    }

    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    private void logFilter(AppEvent<?> appEvent) {
        // 1. Otimização: Fazemos o cast uma única vez
        if (!(appEvent.payload() instanceof LogData data) || appEvent.eventType() == AppEvent.EventType.LOG_OK) return;

        // 2. Cores ANSI
        String RESET = "\u001B[0m";
        String RED = "\u001B[31m";
        String YELLOW = "\u001B[33m";
        String GREEN = "\u001B[32m";
        String CYAN = "\u001B[36m";

        // 3. Timestamp elegante
        String time = LocalTime.now().format(timeFormatter);

        // 4. Switch Expression (Java 17+) - Otimizado e limpo
        switch (appEvent.eventType()) {
            case LOG_ERROR -> printFormat(RED, "ERROR", time, data, RESET);
            case LOG_WARN -> printFormat(YELLOW, "WARN ", time, data, RESET);
            case LOG_OK -> printFormat(GREEN, "OK   ", time, data, RESET);
            default -> { /* Ignora outros eventos como UPDATE */ }
        }
    }

    private void printFormat(String color, String level, String time, LogData data, String reset) {
        // [HH:mm:ss] [LEVEL] [Class] : Message
        System.out.printf("%s[%s] %s%-5s%s \u001B[36m%-20s\u001B[0m : %s%n",
                "\u001B[90m", time, color, level, "\u001B[90m",
                truncateClass(data.className()), data.message());

        // Se for erro e tiver exception, imprime o rastro
        if (data.exception() != null && color.equals("\u001B[31m")) {
            data.exception().getMessage();
        }
    }

    // Otimização visual para não estourar a largura do terminal
    private String truncateClass(String className) {
        return className.length() > 20 ? className.substring(0, 17) + "..." : className;
    }

}
