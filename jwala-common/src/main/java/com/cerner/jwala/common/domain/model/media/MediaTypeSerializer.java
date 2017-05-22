package com.cerner.jwala.common.domain.model.media;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import java.io.IOException;

/**
 * Created by Jedd Cuison on 4/27/2017
 */
public class MediaTypeSerializer extends JsonSerializer<MediaType> {

    @Override
    public void serialize(final MediaType mediaType, final JsonGenerator generator, final SerializerProvider provider)
            throws IOException {
        generator.writeStartObject();
        generator.writeFieldName("name");
        generator.writeString(mediaType.name());
        generator.writeFieldName("displayName");
        generator.writeString(mediaType.getDisplayName());
        generator.writeEndObject();
    }

}
