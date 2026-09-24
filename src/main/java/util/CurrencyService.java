package com.walletwise.util;

import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class CurrencyService {

    // using open.er-api.com because frankfurter doesn't support BDT
    // returns all rates for 1 BDT in one request
    private static final String URL = "https://open.er-api.com/v6/latest/BDT";

    private static Map<String, Double> cache = new HashMap<>();
    private static Instant lastFetch = Instant.MIN;
    private static final long CACHE_MINUTES = 30;

    public static double getRate(String target) throws Exception {

        boolean cacheFresh = false;
        if (lastFetch != Instant.MIN) {
            long mins = Duration.between(lastFetch, Instant.now()).toMinutes();
            if (mins < CACHE_MINUTES) {
                cacheFresh = true;
            }
        }

        if (cacheFresh && cache.containsKey(target)) {
            return cache.get(target);
        }

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(URL))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());

        if (resp.statusCode() != 200) {
            throw new RuntimeException("API error " + resp.statusCode());
        }

        JSONObject json = new JSONObject(resp.body());
        JSONObject ratesObj = json.getJSONObject("rates");

        cache.clear();
        for (String k : ratesObj.keySet()) {
            cache.put(k, ratesObj.getDouble(k));
        }
        lastFetch = Instant.now();

        if (!cache.containsKey(target)) {
            throw new RuntimeException("Currency not found: " + target);
        }

        return cache.get(target);
    }

    public static void clearCache() {
        cache.clear();
        lastFetch = Instant.MIN;
    }
}