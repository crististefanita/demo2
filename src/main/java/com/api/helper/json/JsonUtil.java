package com.api.helper.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

@Log4j
public class JsonUtil {
    public static JSONObject parseJson(String fileJson) {
        JSONParser parser = new JSONParser();
        Object obj = null;
        try {
            obj = parser.parse(new FileReader(fileJson));
        } catch (IOException e) {
            log.error("IOException: " + e.getMessage());
        } catch (ParseException e) {
            log.error("ParseException: " + e.getMessage());
        }
        return (JSONObject) obj;
    }

    public static String toStringJson(Object object) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("JsonProcessingException: " + e.getMessage());
        }
        return null;
    }

    public static <T> T toJson(String jsonString, Class<T> contentClass) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            Reader reader = new StringReader(jsonString);
            return mapper.readValue(reader, contentClass);
        } catch (JsonProcessingException e) {
            log.error("JsonProcessingException: " + e.getMessage());
        } catch (IOException e) {
            log.error("IOException: " + e.getMessage());
        }
        return null;
    }

}
