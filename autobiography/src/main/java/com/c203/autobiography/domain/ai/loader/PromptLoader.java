package com.c203.autobiography.domain.ai.loader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PromptLoader {
    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    public <T> T load(String path, Class<T> clazz) {
        try (InputStream is = getClass().getResourceAsStream("/prompts/" + path)) {
            return yamlMapper.readValue(is, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load prompt file: " + path, e);
        }
    }
}
