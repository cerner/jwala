package com.cerner.jwala.service.balancermanager.impl;

import org.apache.http.Consts;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.util.List;

public class BalancerManagerHttpClient {

    public CloseableHttpResponse doHttpClientPost(final String uri, final List<NameValuePair> nvps) throws IOException {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(uri);
        httppost.setEntity(new UrlEncodedFormEntity(nvps, Consts.UTF_8));
        return httpClient.execute(httppost);
    }
}
