package com.fernandoprado.lhmagent.view;

import com.fernandoprado.lhmagent.Controller.model.AppEvent;
import java.util.concurrent.SubmissionPublisher;

public class ViewPrint {

    SubmissionPublisher<AppEvent<?>> app;

    public ViewPrint(SubmissionPublisher<AppEvent<?>> app2) {
        this.app = app2;
        app2.consume((appEvent -> {

            if (appEvent.eventType() == AppEvent.EventType.UPDATE) {
                System.out.println(appEvent.payload());
            }

        }));
    }
}
