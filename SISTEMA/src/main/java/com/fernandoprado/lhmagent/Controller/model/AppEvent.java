package com.fernandoprado.lhmagent.Controller.model;


public record AppEvent<T>(EventType eventType, T payload) {
    public enum EventType {
        WAKE_UP,
        UPDATE,
        OK,
        ERROR,
        INIT
    }
}
