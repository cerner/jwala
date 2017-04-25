package com.cerner.jwala.common.domain.model;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import java.io.IOException;
import java.nio.file.Path;

/**
 * {@link Path} serializer to a String
 *
 * Created by Jedd Cuison on 12/9/2016
 */
public class PathToStringSerializer extends JsonSerializer<Path> {

    @Override
    public void serialize(final Path path, final JsonGenerator jsonGenerator, final SerializerProvider provider)
            throws IOException {
        jsonGenerator.writeObject(path.toString());
    }

}
