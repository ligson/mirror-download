package org.ligson.mirrordownload.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.ligson.mirrordownload.config.vo.AppConfig;

public class YamlParser {
    public static AppConfig parse() throws Exception {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        return mapper.readValue(YamlParser.class.getResourceAsStream("/app.yaml"), AppConfig.class);
    }

}
