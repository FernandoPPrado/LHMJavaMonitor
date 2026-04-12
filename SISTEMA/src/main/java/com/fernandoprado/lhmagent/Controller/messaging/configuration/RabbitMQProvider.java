package com.fernandoprado.lhmagent.Controller.messaging.configuration;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class RabbitMQProvider {

    private static Connection connection;

    public static void initConnection() throws Exception {
        if (connection != null && connection.isOpen()) {
            return;
        }

        ConnectionFactory factory = new ConnectionFactory();
        factory.setUri("amqps://mwdvyghp:YXpGDCgjw2JsAfQ3_9ZpiSTY0hyUiWm-@jackal.rmq.cloudamqp.com/mwdvyghp");

        connection = factory.newConnection();
        System.out.println("[RabbitMQ] Tubulação TCP mestre estabelecida com sucesso.");
    }


    public static Channel createChannel() throws Exception {
        if (connection == null || !connection.isOpen()) {
            throw new IllegalStateException("Falha: A conexão com o RabbitMQ caiu ou não foi iniciada pelo Service!");
        }
        return connection.createChannel();
    }

    public static void closeConnection() {
        try {
            if (connection != null && connection.isOpen()) {
                connection.close();
                System.out.println("[RabbitMQ] Conexão TCP encerrada com segurança.");
            }
        } catch (Exception e) {
            System.err.println("Erro ao fechar conexão RabbitMQ: " + e.getMessage());
        }
    }
}