package com.fernandoprado.lhmagent.view;

import com.fernandoprado.lhmagent.Controller.model.AppEvent;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.atomic.AtomicReference;

public class TrayView {

    BufferedImage image;
    String s;
    AtomicReference<TrayIcon> atomicIcon;
    SubmissionPublisher<AppEvent<?>> sub;

    public TrayView(SubmissionPublisher<AppEvent<?>> sub) {
        this.sub = sub;
        sub.consume((e) -> {
            System.out.println("OIIIIIIIIIIIII");
            switch (e.eventType()) {
                case OK -> {
                    s = e.payload().toString();

                    if (atomicIcon.get().getToolTip() != null) {
                        EventQueue.invokeLater(() -> {
                            atomicIcon.get().setToolTip(s);
                        });
                    }


                }
                case ERROR -> s = e.payload().toString();
            }

        });
    }

    public void createIcon() throws AWTException {

        try {
            image = ImageIO.read(new File("SISTEMA/src/main/resources/images/sr5zd6da43c2e0aws3.png"));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }


        try {
            if (!SystemTray.isSupported()) return;
            if (image == null) return;
            SystemTray systemTray = SystemTray.getSystemTray();
            PopupMenu pop = new PopupMenu();
            pop.add("CLOSE");
            pop.addActionListener((actionEvent) -> System.exit(0));
            TrayIcon trayIcon = new TrayIcon(image, s, pop);
            atomicIcon = new AtomicReference<>(trayIcon);
            trayIcon.setImageAutoSize(true);
            systemTray.add(trayIcon);


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
