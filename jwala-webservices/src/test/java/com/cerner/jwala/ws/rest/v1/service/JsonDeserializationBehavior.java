package com.cerner.jwala.ws.rest.v1.service;

import org.codehaus.jackson.map.DeserializerProvider;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.deser.CustomDeserializerFactory;
import org.codehaus.jackson.map.deser.StdDeserializerProvider;

import java.util.HashMap;
import java.util.Map;

public class JsonDeserializationBehavior {

    private final Map<Class, JsonDeserializer> mappings;

    public JsonDeserializationBehavior() {
        mappings = new HashMap<>();
    }

    public static String quote(final String aString) {
        return "\"" + aString + "\"";
    }

    public static String key(final String aKey) {
        return quote(aKey) + ":";
    }

    public static String textValue(final String aValue) {
        return quote(aValue);
    }

    public static String keyTextValue(final String aKey,
                                      final String aValue) {
        return key(aKey) + textValue(aValue);
    }

    public static String keyValue(final String aKey,
                                  final String aValue) {
        return key(aKey) + aValue;
    }

    public static String object(final String... someStrings) {
        return "{" + append(someStrings) + "}";
    }

    public static String array(final String... someStrings) {
        return "[" + append(someStrings) + "]";
    }

    public static String append(final String... someStrings) {
        final StringBuilder builder = new StringBuilder();
        String separator = "";
        for (final String string : someStrings) {
            builder.append(separator);
            builder.append(string);
            separator = ",";
        }
        return builder.toString();
    }

    public <T> JsonDeserializationBehavior addMapping(final Class<T> aClass,
                                                          final JsonDeserializer<T> aDeserializer) {
        mappings.put(aClass,
                     aDeserializer);
        return this;
    }

    public ObjectMapper toObjectMapper() {

        final CustomDeserializerFactory factory = createFactory();

        final DeserializerProvider deserializerProvider = new StdDeserializerProvider(factory);

        final ObjectMapper mapper = new ObjectMapper();
        mapper.setDeserializerProvider(deserializerProvider);

        return mapper;
    }

    private CustomDeserializerFactory createFactory() {

        final CustomDeserializerFactory factory = new CustomDeserializerFactory();

        for (final Map.Entry<Class, JsonDeserializer> mapping : mappings.entrySet()) {
            factory.addSpecificMapping(mapping.getKey(),
                                       mapping.getValue());
        }

        return factory;
    }
}
