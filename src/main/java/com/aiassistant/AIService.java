package com.aiassistant;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.intellij.openapi.application.ApplicationManager;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class AIService {

    private static final Gson gson = new Gson();
    private final HttpClient httpClient;

    public AIService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
    }

    public static AIService getInstance() {
        return ApplicationManager.getApplication().getService(AIService.class);
    }

    public void chat(List<ChatMessage> messages, Consumer<String> onFlush, Consumer<Throwable> onError, Runnable onComplete) {
        AISettingsState settings = AISettingsState.getInstance();

        String baseUrl = settings.baseUrl;
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "https://api.openai.com/v1";
        }
        if (!baseUrl.endsWith("/")) {
            baseUrl += "/";
        }
        String apiUrl = baseUrl + "chat/completions";

        String apiKey = settings.apiKey;
        if (apiKey == null || apiKey.isBlank()) {
            onError.accept(new RuntimeException("请先在 Settings → AI Code Assistant 中配置 API Key"));
            return;
        }

        String model = settings.model;
        if (model == null || model.isBlank()) {
            model = "gpt-3.5-turbo";
        }

        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", model);
        requestBody.addProperty("stream", true);

        JsonArray messagesArray = new JsonArray();
        for (ChatMessage msg : messages) {
            JsonObject msgObj = new JsonObject();
            msgObj.addProperty("role", msg.getRole());
            msgObj.addProperty("content", msg.getContent());
            messagesArray.add(msgObj);
        }
        requestBody.add("messages", messagesArray);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(requestBody)))
                .timeout(Duration.ofSeconds(120))
                .build();

        CompletableFuture.runAsync(() -> {
            try {
                HttpResponse<java.io.InputStream> response = httpClient.send(request,
                        HttpResponse.BodyHandlers.ofInputStream());

                if (response.statusCode() != 200) {
                    String errorBody = new String(response.body().readAllBytes());
                    onError.accept(new RuntimeException("API Error " + response.statusCode() + ": " + errorBody));
                    return;
                }

                StringBuilder batch = new StringBuilder();
                long lastFlush = System.currentTimeMillis();
                java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(response.body()));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("data: ")) {
                        String data = line.substring(6);
                        if ("[DONE]".equals(data)) {
                            break;
                        }
                        try {
                            JsonObject json = gson.fromJson(data, JsonObject.class);
                            JsonArray choices = json.getAsJsonArray("choices");
                            if (choices != null && choices.size() > 0) {
                                JsonObject choice = choices.get(0).getAsJsonObject();
                                JsonObject delta = choice.getAsJsonObject("delta");
                                if (delta != null && delta.has("content")) {
                                    String content = delta.get("content").getAsString();
                                    if (content != null) {
                                        batch.append(content);
                                        long now = System.currentTimeMillis();
                                        if (now - lastFlush > 80) {
                                            String flushText = batch.toString();
                                            if (!flushText.isEmpty()) {
                                                onFlush.accept(flushText);
                                            }
                                            batch.setLength(0);
                                            lastFlush = now;
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            // skip malformed SSE line
                        }
                    }
                }
                reader.close();

                if (batch.length() > 0) {
                    onFlush.accept(batch.toString());
                }

                if (onComplete != null) {
                    onComplete.run();
                }
            } catch (Exception e) {
                onError.accept(e);
            }
        });
    }

    public String chatSync(List<ChatMessage> messages) throws Exception {
        AISettingsState settings = AISettingsState.getInstance();

        String baseUrl = settings.baseUrl;
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "https://api.openai.com/v1";
        }
        if (!baseUrl.endsWith("/")) {
            baseUrl += "/";
        }
        String apiUrl = baseUrl + "chat/completions";

        String apiKey = settings.apiKey;
        if (apiKey == null || apiKey.isBlank()) {
            throw new RuntimeException("请先在 Settings → AI Code Assistant 中配置 API Key");
        }

        String model = settings.model;
        if (model == null || model.isBlank()) {
            model = "gpt-3.5-turbo";
        }

        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", model);
        requestBody.addProperty("stream", false);

        JsonArray messagesArray = new JsonArray();
        for (ChatMessage msg : messages) {
            JsonObject msgObj = new JsonObject();
            msgObj.addProperty("role", msg.getRole());
            msgObj.addProperty("content", msg.getContent());
            messagesArray.add(msgObj);
        }
        requestBody.add("messages", messagesArray);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(requestBody)))
                .timeout(Duration.ofSeconds(120))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("API Error " + response.statusCode() + ": " + response.body());
        }

        JsonObject json = gson.fromJson(response.body(), JsonObject.class);
        JsonArray choices = json.getAsJsonArray("choices");
        if (choices != null && choices.size() > 0) {
            JsonObject choice = choices.get(0).getAsJsonObject();
            JsonObject message = choice.getAsJsonObject("message");
            if (message != null && message.has("content")) {
                return message.get("content").getAsString();
            }
        }

        throw new RuntimeException("No content in API response");
    }
}