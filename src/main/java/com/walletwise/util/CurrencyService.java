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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class CurrencyService {

    private static final String URL = "https://open.er-api.com/v6/latest/BDT";

    private static ExecutorService pool = Executors.newFixedThreadPool(3);

    private static Map<String, Double> cache = new HashMap<>();
    private static Instant lastFetch = Instant.MIN;

    public static double getRate(String target) throws Exception {
        if (isCacheFresh() && cache.containsKey(target)) {
            return cache.get(target);
        }
        fetchAndCache();
        if (!cache.containsKey(target)) {
            throw new RuntimeException("Currency not found: " + target);
        }
        return cache.get(target);
    }

    public static void getRateAsync(String target, Consumer<Double> onSuccess, Consumer<String> onError) {
        pool.submit(() -> {
            try {
                double rate = getRate(target);
                onSuccess.accept(rate);
            } catch (Exception e) {
                onError.accept(e.getMessage());
            }
        });
    }

    private static boolean isCacheFresh() {
        if (lastFetch == Instant.MIN) return false;
        long mins = Duration.between(lastFetch, Instant.now()).toMinutes();
        return mins < 30;
    }

    private static void fetchAndCache() throws Exception {
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
        JSONObject rates = json.getJSONObject("rates");

        cache.clear();
        for (String k : rates.keySet()) {
            cache.put(k, rates.getDouble(k));
        }
        lastFetch = Instant.now();
    }

    public static void shutdown() {
        pool.shutdown();
    }
}