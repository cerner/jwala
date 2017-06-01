package com.cerner.jwala.common.domain.model.media;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;

import java.io.IOException;

/**
 * Created on 6/1/2017.
 */
public class StringToMediaType extends JsonDeserializer<MediaType> {
    @Override
    public MediaType deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        return MediaType.valueOf(jp.getText());
    }
}
