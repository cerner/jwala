package com.cerner.jwala.common.domain.model.ssh;

import com.cerner.jwala.common.properties.ApplicationProperties;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

public class DecryptPassword {

    private final String decryptExpressionString = ApplicationProperties.get("decryptExpression");
    private final String encryptExpressionString = ApplicationProperties.get("encryptExpression");

    private final String decryptorImpl;
    private final String encryptorImpl;

    public DecryptPassword() {
        decryptorImpl = decryptExpressionString;
        encryptorImpl = encryptExpressionString;
    }

    public DecryptPassword(String encryptImpl, String decryptImpl) {
        encryptorImpl = encryptImpl;
        decryptorImpl = decryptImpl;
    }

    public String decrypt(char[] encyptedValue) {
        return decrypt(String.valueOf(encyptedValue));
    }

    public String decrypt(String encryptedValue) {
        if (encryptedValue==null) {
            return null;
        }
        
        final ExpressionParser expressionParser = new SpelExpressionParser();
        final Expression decryptExpression = expressionParser.parseExpression(decryptorImpl);

        final StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("stringToDecrypt", encryptedValue);
        return decryptExpression.getValue(context, String.class);
    }
    
    public String encrypt(String unencryptedValue) {
        
        if (unencryptedValue==null) {
            return null;
        }
        
        final ExpressionParser expressionParser = new SpelExpressionParser();
        final Expression encryptExpression = expressionParser.parseExpression(encryptorImpl);

        final StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("stringToEncrypt", unencryptedValue);
        return encryptExpression.getValue(context, String.class);
    }
}
