package com.example;

import com.alibaba.fastjson.JSON;
import com.example.Response.GptResponse;
import com.example.ai.Answer;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.example.Response.tokenResponse;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.ServerException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

@Component
public class tokenTask {
    public static String token = null;
    // 一小时更新一次token
    @Scheduled(fixedDelay = 3600000)
    public void resetToken(){
        System.out.println("Reset token...");
        String newToken = getToken();
        if (newToken != null) {
            token = newToken;
        }
        else {
            System.out.println("Failed to get token!");
        }
        System.out.println("New Token: " + token);
    }

    private String getToken() {
        try (CloseableHttpClient httpClient = HttpClientBuilder.create()
                .setSSLSocketFactory(getSslConnectionSocketFactory())
                .build()) {
            HttpPost post = getHttpPost();
            CloseableHttpResponse response;
            try {
                response = httpClient.execute(post);
                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    String responseJson = EntityUtils.toString(response.getEntity());
                    tokenResponse token = JSON.parseObject(responseJson, tokenResponse.class);
                    return token.getAccess_token();
                }
            } catch (SocketTimeoutException e) {
                System.out.println("-- warning: Read timed out!");
            } catch (SocketException e) {
                System.out.println("-- warning: Connection reset!");
            } catch (Exception e) {
                System.out.println("-- warning: Please try again!");
            }
        } catch (IOException | NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
    private static HttpPost getHttpPost() throws IOException {
        String url = "https://aip.baidubce.com/oauth/2.0/token?grant_type=client_credentials&client_id=【API Key】&client_secret=【Secret Key】";
        Properties prop =new Properties();
        InputStream inputStream = Files.newInputStream(Paths.get("src/main/resources/application.properties"));
        prop.load(inputStream);
        String connectTimeout = prop.getProperty("connectTimeout");
        String connectionRequestTimeout = prop.getProperty("connectionRequestTimeout");
        String socketTimeout = prop.getProperty("socketTimeout");
        url = url.replace("【API Key】", prop.getProperty("api_key"));
        url = url.replace("【Secret Key】", prop.getProperty("api_secret"));
        HttpPost post = new HttpPost(url);
        post.addHeader("Content-Type", "application/json");
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(Integer.parseInt(connectTimeout)).setConnectionRequestTimeout(Integer.parseInt(connectionRequestTimeout))
                .setSocketTimeout(Integer.parseInt(socketTimeout)).build();
        post.setConfig(requestConfig);
        return post;
    }
    private static SSLConnectionSocketFactory getSslConnectionSocketFactory() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        TrustStrategy acceptingTrustStrategy = (x509Certificates, s) -> true;
        SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
        return new SSLConnectionSocketFactory(sslContext, new NoopHostnameVerifier());
    }
}
