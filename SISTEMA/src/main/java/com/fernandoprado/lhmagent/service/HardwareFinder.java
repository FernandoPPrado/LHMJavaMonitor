package com.fernandoprado.lhmagent.service;


import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HardwareFinder {

    public HardwareBusca hardwareBusca = new HardwareBusca();

    public Map<String, String> lerValoresAtuais(JsonNode node, Map<String, String> mapaDePaths) {
        Map<String, String> resultado = new HashMap<>();

        mapaDePaths.forEach((nome, path) -> {

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
