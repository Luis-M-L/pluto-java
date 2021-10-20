package com.example.pluto.bitfinex;

import com.example.pluto.exchanges.ExchangeAPIClient;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.stereotype.Component;

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
public class BitfinexAPIClient extends ExchangeAPIClient {

    public static String publicUrl = "https://api-pub.bitfinex.com";
    public static String authUrl = "https://api.bitfinex.com";

    public BitfinexAPIClient() {
        super();
    }

    @Override
    public boolean checkStatus() {
        return false;
    }

    @Override
    public String publicGet(List<String> subpath, Map<String, String> params) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                                        .uri(URI.create(buildUri(publicUrl, subpath, params)))
                                        .build();
        HttpResponse<String> response = super.getClient().send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    @Override
    public String authPost(List<String> subpath, Map<String, String> params, String body) throws IOException, InterruptedException {
        URI uri = URI.create(buildUri(authUrl, subpath, params));
        HttpRequest request = HttpRequest.newBuilder()
                                        .uri(uri)
                                        .headers(buildHeaders(uri.getRawPath(), body))
                                        .POST(HttpRequest.BodyPublishers.ofString(body))
                                        .build();
        HttpResponse<String> response = super.getClient().send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    @Override
    public String buildUri(String basepath, List<String> subpath, Map<String, String> params) {
        StringBuilder sb = new StringBuilder(basepath);
        subpath.forEach(s -> sb.append("/").append(s));
        if (!params.keySet().isEmpty()) {
            sb.append("?");
            params.keySet().forEach(k -> sb.append(k).append("=").append(params.get(k)));
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
        String keyPath;
        if (isPublic){
            keyPath = "../../k";
        } else {
            keyPath = "../../sk";
        }
        return new String(Files.readAllBytes(Paths.get(keyPath)));
    }

}
