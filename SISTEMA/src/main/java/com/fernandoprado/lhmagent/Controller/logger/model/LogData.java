package com.fernandoprado.lhmagent.Controller.logger.model;

public record LogData(String className, String message, Throwable exception) {
}
