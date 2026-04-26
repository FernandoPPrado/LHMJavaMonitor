package com.fernandoprado.lhmagent.Controller.messaging.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fernandoprado.lhmagent.Controller.enviroment.EnvConfiguration;
import com.fernandoprado.lhmagent.Controller.logger.model.LogData;
import com.fernandoprado.lhmagent.Controller.messaging.configuration.RabbitMQProvider;
import com.fernandoprado.lhmagent.Controller.model.AppEvent;
import com.rabbitmq.client.Channel;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.Map;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.atomic.AtomicBoolean;

public class MessagingService {

    private SubmissionPublisher<AppEvent<?>> submissionPublisher;
    private Channel channel;
    private final ObjectMapper objectMapper = new ObjectMapper();
    AtomicBoolean locked = new AtomicBoolean(false);

    public MessagingService(SubmissionPublisher<AppEvent<?>> sub) {
        this.submissionPublisher = sub;
        initServer();
        submissionPublisher.consume(this::processAppEvent);
    }

    public void initServer() {
        try {
            RabbitMQProvider.initConnection();
            channel = RabbitMQProvider.createChannel();
            channel.queueDeclare("MinhaFila", true, false, false, null);
            submissionPublisher
                    .submit(new AppEvent<>(AppEvent.EventType.LOG_OK, new LogData(MessagingService.class.getSimpleName(), "CONEXAO ESTABELECIDA", null)));

        } catch (Exception e) {
            submissionPublisher
                    .submit(new AppEvent<>(AppEvent.EventType.LOG_ERROR, new LogData(MessagingService.class.getSimpleName(), "FALHA AO DECLARAR CONEXAO COM RABBIT", e)));

        }

    }

    public void sendMessage(Map<String, String> mapEvent) {

        try {
            if (channel == null || !channel.isOpen()) {
                submissionPublisher
                        .submit(new AppEvent<>(AppEvent.EventType.LOG_WARN, new LogData(MessagingService.class.getSimpleName(), "CANAL FECHADO", null)));
            } else {
                String jsonPayload = objectMapper.writeValueAsString(mapEvent);
                channel.basicPublish("", "MinhaFila", null, jsonPayload.getBytes(StandardCharsets.UTF_8));
                submissionPublisher
                        .submit(new AppEvent<>(AppEvent.EventType.LOG_OK, new LogData(MessagingService.class.getSimpleName(), "MENSAGEM ENVIADA", null)));
            }

        } catch (Exception e) {
            submissionPublisher
                    .submit(new AppEvent<>(AppEvent.EventType.LOG_ERROR, new LogData(MessagingService.class.getSimpleName(), "FALHA AO ENVIAR MENSAGEM", e)));

        } finally {
            locked.set(false);

        }
    }

    public void processAppEvent(AppEvent<?> appEvent) {

        if (appEvent.eventType() == AppEvent.EventType.UPDATE) {
            if (locked.compareAndSet(false, true))
                sendMessage((Map<String, String>) appEvent.payload());

        }

    }
}
