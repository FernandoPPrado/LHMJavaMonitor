package com.fernandoprado.lhmagent.Controller.model;


public record AppEvent<T>(EventType eventType, T payload) {
    public enum EventType {
        WARN,
        UPDATE,
        OK,
        LOG_OK,
        LOG_WARN,
        ERROR,
        LOG_ERROR,
        INIT
    }
}
