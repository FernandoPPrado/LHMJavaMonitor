package com.fernandoprado.lhmagent.Controller.service;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fernandoprado.lhmagent.Controller.enviroment.EnvConfiguration;
import com.fernandoprado.lhmagent.Controller.model.Sensor;

import java.util.*;
import java.util.concurrent.SubmissionPublisher;
import java.util.stream.Collectors;

public class HardwareBusca {


    public List<Sensor> sensorList = List.of(new Sensor(EnvConfiguration.CPU_TEMP_KEYWORD, "Temperature"),
            new Sensor(EnvConfiguration.CPU_LOAD_KEYWORD, "Load"),
            new Sensor(EnvConfiguration.GPU_TEMP_KEYWORD, "Temperature"),
            new Sensor(EnvConfiguration.MEMORY_USAGE, "Load"));

    public String mapearTudo(JsonNode node, String path, Sensor alvo) {

        if (node == null) return null;
        String name = node.path("Text").asText();
        String type = node.path("Type").asText();

        //IGNORAR A MEMORIA VIRTUAL, SERÁ REFATORADO
        if ("Virtual Memory".equalsIgnoreCase(name)) {
            return null;
        }

        if (alvo.name().equalsIgnoreCase(name) && alvo.type().equalsIgnoreCase(type)) {
            return path + "/Value";
        }

        if (node.has("Children") && node.get("Children").isArray()) {
            JsonNode children = node.get("Children");
            for (int i = 0; i < children.size(); i++) {

                String resultado = mapearTudo(children.get(i), path + "/Children/" + i, alvo);

                if (resultado != null) return resultado;

            }
        }
        return null;

    }


    public Map<String, JsonPointer> encontrarPath(JsonNode rootNode) {
        Map<String, String> mapaString = sensorList.stream().collect(Collectors.toMap(Sensor::name, sensor -> {
            String path = mapearTudo(rootNode, "", sensor);
            if (path != null) {
                return path;
            } else return "0";
        }));

        return mapaString.entrySet().stream().filter(
                        entry -> entry.getValue().startsWith("/"))
                .collect(Collectors.toMap(entry -> entry.getKey(), entry -> JsonPointer.compile(entry.getValue())
                        )
                );

    }


}
