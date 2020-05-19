package com.sansarip.st8m8;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.sansarip.st8m8.Utilities.execCmd;

public class Parse {
    public static Map<String, Map<String, String>> toHashMap(String json) throws IOException {
         return (Map<String, Map<String, String>>) new ObjectMapper().readValue(json, Map.class);

    }

    public static Map<String, Map<String, String>> parse(String content) {
        try {
            String json = execCmd(String.format("./bb.sh \"%s\"", content)).replaceAll("\\\\\"", "\"").replaceAll("^\"+|\"+$", "");
            return toHashMap(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new HashMap<>();
    }
}
