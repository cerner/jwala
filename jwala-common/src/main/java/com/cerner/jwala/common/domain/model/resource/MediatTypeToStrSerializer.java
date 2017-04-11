package com.cerner.jwala.common.domain.model.resource;

import org.apache.tika.mime.MediaType;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;

import java.io.IOException;

/**
 * Created on 4/11/2017.
 */
public class MediatTypeToStrSerializer extends JsonSerializer<MediaType> {

    @Override
    public void serialize(MediaType value, org.codehaus.jackson.JsonGenerator jgen, org.codehaus.jackson.map.SerializerProvider provider) throws IOException, JsonProcessingException {
        if (null == value) {
            jgen.writeString("");
        } else {
            jgen.writeString(value.toString());
        }
    }

}
