package com.reliaquest.api.common;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class JsonUtil {
    public static String loadJson(String fileName) throws Exception {
        return new String(
                Files.readAllBytes(Paths.get("src/test/resources/testdata/" + fileName)), StandardCharsets.UTF_8);
    }
}
