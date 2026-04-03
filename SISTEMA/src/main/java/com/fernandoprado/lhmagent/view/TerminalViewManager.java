package com.fernandoprado.lhmagent.view;

import com.fernandoprado.lhmagent.Controller.model.AppEvent;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.atomic.AtomicBoolean;

public class TerminalViewManager {

    Terminal terminal = new DefaultTerminalFactory().createTerminal();
    Screen screen = new TerminalScreen(terminal);
    TextGraphics textGraphics;
    AtomicBoolean busy = new AtomicBoolean(false);


    public TerminalViewManager() throws IOException {

    }

    public void startScreen() throws IOException {
        screen.startScreen();
        textGraphics = screen.newTextGraphics();
    }

    public void updateScreen(Map<String, String> dados) throws IOException {
        if (busy.compareAndSet(false, true)) {
            try {
                screen.clear();
                textGraphics.putString(1, 1, dados.toString());
                screen.refresh();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                busy.set(false);
            }
        } else {
            System.out.println("OCUPADO");
            return;
        }


    }

    public void closeScreen() throws IOException {
        screen.close();
    }

}
