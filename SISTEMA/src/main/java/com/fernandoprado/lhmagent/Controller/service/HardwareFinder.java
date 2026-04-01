package com.fernandoprado.lhmagent.Controller.service;


import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.Map;

public class HardwareFinder {

    public HardwareBusca hardwareBusca = new HardwareBusca();

    public Map<String, String> lerValoresAtuais(JsonNode node, Map<String, JsonPointer> mapaDePointers) {
        Map<String, String> resultado = new HashMap<>();

        mapaDePointers.forEach((nome, path) -> {

            if (path.equals("0")) return;
            JsonNode valorNode = node.at(path);

            if (!valorNode.isMissingNode()) {
                resultado.put(nome, valorNode.asText());
            } else {
                System.out.println("NAO ACHOU");
                return;
            }

        });
        return resultado;
    }


}
