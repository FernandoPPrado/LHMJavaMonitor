package com.fernandoprado.lhmagent.client;


import com.fasterxml.jackson.databind.JsonNode;
import feign.RequestLine;

public interface LhmClient {

    @RequestLine("GET /data.json")
    JsonNode getHardwareData();
}
