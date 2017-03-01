package com.cerner.jwala.ws.rest.v1.json;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.JsonDeserializer;

import java.util.HashSet;
import java.util.Set;

public abstract class AbstractJsonDeserializer<U> extends JsonDeserializer<U> {

    protected Set<String> deserializeGroupIdentifiers(final JsonNode aNode) {

        return deserializeIdentifiers("groupIds",
                                      "groupId",
                                      aNode);
    }

    protected Set<String> deserializeJvmIdentifiers(final JsonNode aNode) {

        return deserializeIdentifiers("jvmIds",
                                      "jvmId",
                                      aNode);
    }

    private Set<String> deserializeIdentifiers(final String aMultipleIdentifierKey,
                                               final String aSingleIdentifierKey,
                                               final JsonNode aRootNode) {

        final Set<String> results = new HashSet<>();
        final JsonNode multiNode = aRootNode.get(aMultipleIdentifierKey);

        if (multiNode != null) {
            for (final JsonNode node : multiNode) {
                results.add(node.get(aSingleIdentifierKey).getValueAsText());
            }
        } else {
            final JsonNode singleNode = aRootNode.get(aSingleIdentifierKey);
            if ( singleNode != null) {
                results.add(singleNode.getValueAsText());
            }
        }

        return results;
    }

}
