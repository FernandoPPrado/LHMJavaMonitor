package com.fernandoprado.lhmagent.view;

import com.fernandoprado.lhmagent.Controller.core.LhmProcessManager;
import com.fernandoprado.lhmagent.Controller.logger.model.LogData;
import com.fernandoprado.lhmagent.Controller.messaging.configuration.RabbitMQProvider;
import com.fernandoprado.lhmagent.Controller.model.AppEvent;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.atomic.AtomicReference;

public class TrayView {

    BufferedImage image;
    AtomicReference<TrayIcon> atomicIcon = new AtomicReference<>();
    String s = "LHM Agent - Iniciando...";
    SubmissionPublisher<AppEvent<?>> sub;

    public TrayView(SubmissionPublisher<AppEvent<?>> sub) {
        this.sub = sub;
        createIcon();
        sub.consume(this::updateCaption);
    }


    public void createIcon() {

        try {
            InputStream is = getClass().getResourceAsStream("/images/sr5zd6da43c2e0aws3.png");
            if (is == null) {
                sub.submit(new AppEvent<LogData>(AppEvent.EventType.LOG_ERROR, new LogData(TrayView.class.getSimpleName(), "IMAGEM INEXISTENTE", new IllegalArgumentException())));
                return;
            }
            image = ImageIO.read(is);

        } catch (IOException e) {
            sub.submit(new AppEvent<>(AppEvent.EventType.LOG_ERROR, new LogData(TrayView.class.getSimpleName(), "FALHA AO LER OS BYTES DA IMAGEM", e)));
            return;
        }


        try {
            if (!SystemTray.isSupported()) {
                sub.submit(new AppEvent<>(AppEvent.EventType.LOG_ERROR, new LogData(TrayView.class.getSimpleName(), "TRAY VIEW NAO SUPORTADO", new IOException())));
                return;
            }
            if (image == null) {
                sub.submit(new AppEvent<>(AppEvent.EventType.LOG_ERROR, new LogData(TrayView.class.getSimpleName(), "FALHA AO LER OS BYTES DA IMAGEM", new IOException())));
                return;
            }
            SystemTray systemTray = SystemTray.getSystemTray();
            PopupMenu pop = new PopupMenu();
            pop.add("CLOSE");
            pop.addActionListener((actionEvent) -> {

                try {
                    RabbitMQProvider.closeConnection();
                    LhmProcessManager.closeLhm();
                    System.exit(0);
                } catch (Exception e) {
                    sub.submit(new AppEvent<>(AppEvent.EventType.LOG_ERROR, new LogData(TrayView.class.getSimpleName(), "FALHA FINALIZAR O PROGRAMA", new IOException())));
                }


            });

            TrayIcon trayIcon = new TrayIcon(image, s, pop);
            atomicIcon.set(trayIcon);
            trayIcon.setImageAutoSize(true);
            systemTray.add(trayIcon);


        } catch (Exception e) {
            sub.submit(new AppEvent<>(AppEvent.EventType.LOG_ERROR, new LogData(TrayView.class.getSimpleName(), "FALHA AO CRIAR A TRAY VIEW", e)));
        }

    }

    private void updateCaption(AppEvent<?> e) {
        if (e.eventType() == AppEvent.EventType.OK || e.eventType() == AppEvent.EventType.ERROR) {
            s = e.eventType().toString();
            if (atomicIcon.get() != null) {
                EventQueue.invokeLater(() -> {
                    atomicIcon.get().setToolTip(s);
                });
            }

        }
    }

}
