package com.fernandoprado.lhmagent.Controller.messaging.service;

import com.fasterxml.jackson.databind.ObjectMapper;
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
        submissionPublisher.consume(appEvent -> {
            System.out.println("Chegouu");
            if (appEvent.eventType() == AppEvent.EventType.UPDATE) {
                try {
                    if (locked.compareAndSet(false, true))
                        sendMessage((Map<String, String>) appEvent.payload());
                } catch (Exception e) {
                    e.printStackTrace();
                    locked.set(false);
                }
            }
        });
    }

    public void initServer() {
        try {
            RabbitMQProvider.initConnection();
            channel = RabbitMQProvider.createChannel();
            channel.queueDeclare("MinhaFila", true, false, false, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void sendMessage(Map<String, String> mapEvent) {

        try {
            if (channel == null || !channel.isOpen()) {
                System.out.println("Canal Fechado");
            } else {
                String jsonPayload = objectMapper.writeValueAsString(mapEvent);
                channel.basicPublish("", "MinhaFila", null, jsonPayload.getBytes(StandardCharsets.UTF_8));
                System.out.println("Mensagem enviada");
            }

        } catch (Exception e) {
            System.out.printf("Erro ao enviar mensagem {%s}", e.getMessage());

        } finally {
            locked.set(false);

        }
    }

}
