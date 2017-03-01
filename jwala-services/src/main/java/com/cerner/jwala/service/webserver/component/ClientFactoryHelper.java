package com.cerner.jwala.service.webserver.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

@Service
public class ClientFactoryHelper {

    @Autowired
    @Qualifier("httpRequestFactory")
    private HttpComponentsClientHttpRequestFactory httpClientFactory;

    public ClientHttpResponse requestGet(URI statusUri) throws IOException {
        ClientHttpRequest request = httpClientFactory.createRequest(statusUri, HttpMethod.GET);
        return request.execute();
    }

    public HttpsURLConnection getHttpsURLConnection(final String urlStr) throws IOException {
        final URL url = new URL(urlStr);
        return (HttpsURLConnection) url.openConnection();
    }

}
