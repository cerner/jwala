package com.cerner.jwala.common.domain.model;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * De-serializes a String to a {@link Path}
 *
 * Created by Jedd Cuison on 12/9/2016
 */
public class StringToPathDeserializer extends JsonDeserializer<Path> {

    @Override
    public Path deserialize(final JsonParser jsonParser, final DeserializationContext deserializationContext) throws IOException {
        return Paths.get(jsonParser.getText());
    }

}
