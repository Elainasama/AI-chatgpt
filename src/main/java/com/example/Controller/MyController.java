package com.example.Controller;

import com.alibaba.fastjson.JSON;
import com.example.Request.GptRequest;
import com.example.Response.GptResponse;
import com.example.ai.Answer;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.example.tokenTask;
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

@RestController
public class MyController {
    @PostMapping("/chatgpt")
    public GptResponse processRequest(@RequestBody GptRequest request) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {
        // 处理请求并生成响应
        System.out.println("Request: " + request.getContent());
        try (CloseableHttpClient httpClient = HttpClientBuilder.create()
                .setSSLSocketFactory(getSslConnectionSocketFactory())
                .build()) {
            String content = request.getContent();
            HttpPost post = getHttpPost();
            StringEntity stringEntity = new StringEntity(getRequestJson(content), getContentType());
            post.setEntity(stringEntity);
            CloseableHttpResponse response;
            try {
                response = httpClient.execute(post);
                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    String responseJson = EntityUtils.toString(response.getEntity());
                    Answer answer = JSON.parseObject(responseJson, Answer.class);
                    if (answer.getResult() == null) {
                        return new GptResponse(500, null);
                    }
                    return new GptResponse(200, answer.getResult());
                } else if (response.getStatusLine().getStatusCode() == 429) {
                    System.out.println("-- warning: Too Many Requests!");
                } else if (response.getStatusLine().getStatusCode() == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                    throw new ServerException("------ Server error, program terminated! ------");
                } else {
                    System.out.println("-- warning: Error, please try again!");
                }
            } catch (SocketTimeoutException e) {
                System.out.println("-- warning: Read timed out!");
            } catch (SocketException e) {
                System.out.println("-- warning: Connection reset!");
            } catch (Exception e) {
                System.out.println("-- warning: Please try again!");
            }
        }
        return new GptResponse(500, null);
    }

    private static ContentType getContentType() {
        return ContentType.create("text/json", "UTF-8");
    }

    private static String getRequestJson(String question) {
        return "{\"messages\":[{\"role\": \"user\",\"content\": \"请扮演以下角色，你是净土的守护者“净夏莱”，一个富有智慧的白发老头，你会好好倾听我的问题并为我提供你所有力所能及的帮助,请不要说出你在扮演，现在我问你:" + question + "\"}]}";
    }

    private static HttpPost getHttpPost() throws IOException {
        Properties prop =new Properties();
        InputStream inputStream = Files.newInputStream(Paths.get("src/main/resources/application.properties"));
        prop.load(inputStream);
        String connectTimeout = prop.getProperty("connectTimeout");
        String connectionRequestTimeout = prop.getProperty("connectionRequestTimeout");
        String socketTimeout = prop.getProperty("socketTimeout");
        String token = tokenTask.token;
        HttpPost post = new HttpPost("https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/completions?access_token=" + token);
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
