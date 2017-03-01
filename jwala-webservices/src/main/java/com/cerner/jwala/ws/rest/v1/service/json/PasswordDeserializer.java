package com.cerner.jwala.ws.rest.v1.service.json;

import com.cerner.jwala.common.domain.model.ssh.DecryptPassword;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;

import java.io.IOException;

/**
 * Encrypts a password
 *
 * Created by JC043760 on 1/15/2017
 */
public class PasswordDeserializer extends JsonDeserializer<String> {

    @Override
    public String deserialize(final JsonParser jsonParser, final DeserializationContext deserializationContext) throws IOException {
        if (StringUtils.isNotEmpty(jsonParser.getText())) {
            return new DecryptPassword().encrypt(jsonParser.getText());
        }
        return StringUtils.EMPTY;
    }

}
