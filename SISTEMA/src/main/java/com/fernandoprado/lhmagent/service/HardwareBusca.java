package com.fernandoprado.lhmagent.service;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.*;
import java.util.stream.Collectors;

public class HardwareBusca {

    public List<String> listaSesores = List.of("CPU Total");


    public String mapearTudo(JsonNode node, String path, String alvo) {

        if (node == null) return null;

        if (node.has("Text") && node.get("Text").asText().equalsIgnoreCase(alvo)) {

            //Teremos um log aqui
            System.out.printf(path + "/Value");
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


    public Map<String, String> encontrarPath(JsonNode rootNode) {
        return listaSesores.stream()
                .collect(Collectors.toMap(
                        nome -> nome,
                        nome -> mapearTudo(rootNode, "", nome)
                ));
    }


}
