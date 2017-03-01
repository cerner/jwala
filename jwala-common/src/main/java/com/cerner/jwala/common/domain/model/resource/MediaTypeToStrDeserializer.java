package com.cerner.jwala.common.domain.model.resource;

import org.apache.tika.mime.MediaType;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.deser.std.StdDeserializer;

import java.io.IOException;

/**
 * Custom JSON deserializer from content type string to {@link MediaType}
 *
 * Created by Jedd Cuison on 10/6/2016
 */
public class MediaTypeToStrDeserializer extends StdDeserializer<MediaType> {

    // This constructor is required because we extended StdDeserializer
    public MediaTypeToStrDeserializer() {
        this(null);
    }

    protected MediaTypeToStrDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public MediaType deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        return MediaType.parse(jp.getText());
    }

}
