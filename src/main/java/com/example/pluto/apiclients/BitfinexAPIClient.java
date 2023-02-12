package com.example.pluto.apiclients;

import com.example.pluto.exchanges.ExchangeAPIClient;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class BitfinexAPIClient extends ExchangeAPIClient {

    Logger LOG = LoggerFactory.getLogger(BitfinexAPIClient.class);

    private String vault;

    public static String publicUrl = "https://api-pub.bitfinex.com";
    public static String authUrl = "https://api.bitfinex.com";

    public BitfinexAPIClient() {
        super();
        vault = System.getenv("API_KEYSTORE");
    }

    @Override
    public boolean checkStatus() {
        return false;
    }

    @Override
    public HttpResponse publicGet(List<String> subpath, Map<String, String> params) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                                        .uri(URI.create(buildUri(publicUrl, subpath, params)))
                                        .build();
        return send(request);
    }

    @Override
    public HttpResponse authPost(List<String> subpath, Map<String, String> params, String body) {
        LOG.info("Request to: " + "[subpath]" + subpath + "[params]" + params + "[body]" + body);
        URI uri = URI.create(buildUri(authUrl, subpath, params));
        HttpRequest request = HttpRequest.newBuilder()
                                        .uri(uri)
                                        .headers(buildHeaders(uri.getRawPath(), body))
                                        .POST(HttpRequest.BodyPublishers.ofString(body))
                                        .build();

        return send(request);
    }

    @Override
    public HttpResponse send(HttpRequest request) {
        HttpResponse<String> response = null;
        try {
            response = super.getClient().send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return response;
    }

    @Override
    public String buildUri(String basepath, List<String> subpath, Map<String, String> params) {
        StringBuilder sb = new StringBuilder(basepath);
        subpath.forEach(s -> sb.append("/").append(s));
        if (!params.keySet().isEmpty()) {
            sb.append("?");
            params.keySet().forEach(k -> sb.append(k).append("=").append(params.get(k)).append("&"));
        }
        return sb.toString();
    }

    private String[] buildHeaders(String path, String body) {
        String[] headers = new String[8];
        String nonce = String.valueOf(System.currentTimeMillis());

        try {
            headers[0] = "Content-Type";
            headers[1] = "application/json";
            headers[2] = "bfx-nonce";
            headers[3] = nonce;
            headers[4] = "bfx-apikey";
            headers[5] = getKey(true);
            headers[6] = "bfx-signature";
            headers[7] = getSignature(path, body, nonce);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return headers;
    }

    private String getSignature(String path, String body, String nonce) throws IOException, URISyntaxException {
        String signature = new StringBuilder().append("/api").append(path).append(nonce).append(body).toString();
        HmacUtils hmacUtils = getHmacEncrypter();
        return hmacUtils.hmacHex(signature);
    }

    private HmacUtils getHmacEncrypter() throws IOException, URISyntaxException {
        String key = getKey(false);
        return new HmacUtils(HmacAlgorithms.HMAC_SHA_384, key);
    }

    private String getKey(boolean isPublic) throws URISyntaxException, IOException {
        String keyPath = System.getenv("API_KEYSTORE");
        if (isPublic){
            keyPath = vault + File.separator + "k";
        } else {
            keyPath = vault + File.separator + "sk";
        }
        return new String(Files.readAllBytes(Paths.get(keyPath)));
    }

}
