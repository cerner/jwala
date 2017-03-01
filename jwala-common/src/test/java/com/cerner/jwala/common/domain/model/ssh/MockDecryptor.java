package com.cerner.jwala.common.domain.model.ssh;

public class MockDecryptor {

    public String decryptBase64(String enc) {
        return "DECRYPT:" + enc;
    }

    public String encryptToBase64(String enc) {
        return "ENCRYPT:" + enc;
    }

}
