package com.testmu.llm;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

public class LlmFailureExplainer {
    // Chosen Option A because it is more useful during a failing test run than classifying failures after the fact.
    private final String apiKey;
    private final String model;
    private final String baseUrl;
    private final Path outputPath;

    public LlmFailureExplainer() {
        this(
                System.getenv("OPENAI_API_KEY"),
                System.getenv().getOrDefault("OPENAI_MODEL", "gpt-4o-mini"),
                System.getenv().getOrDefault("OPENAI_BASE_URL", "https://api.openai.com/v1"),
                Paths.get("target", "llm-reports", "failure-explanation.json")
        );
    }

    public LlmFailureExplainer(String apiKey, String model, String baseUrl, Path outputPath) {
        this.apiKey = apiKey;
        this.model = model;
        this.baseUrl = baseUrl;
        this.outputPath = outputPath;
    }

    public Path getOutputPath() {
        return outputPath;
    }

    public String explainFailure(String testName, String pageState, String apiResponse) throws Exception {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("OPENAI_API_KEY is required for a real LLM failure explanation. Configure it before running Task 3.");
        }

        String prompt = String.format(Locale.US,
                "You are a QA assistant for TestMu. Explain this failure in plain English and suggest one concrete fix.\n" +
                        "Test: %s\nPage state: %s\nAPI response: %s",
                testName,
                pageState,
                apiResponse);

        String explanation = callOpenAi(prompt);
        JSONObject report = new JSONObject();
        report.put("testName", testName);
        report.put("source", "openai");
        report.put("response", explanation);
        report.put("pageState", pageState);
        report.put("apiResponse", apiResponse);

        Files.createDirectories(outputPath.getParent());
        Files.writeString(outputPath, report.toString(2));
        return explanation;
    }

    private String callOpenAi(String prompt) throws Exception {
        URL url = new URL(baseUrl + "/chat/completions");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Bearer " + apiKey);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        JSONObject payload = new JSONObject();
        payload.put("model", model);
        JSONArray messages = new JSONArray();
        JSONObject systemMessage = new JSONObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", "You are a TestMu QA assistant. Reply with plain English and one specific fix suggestion.");
        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);
        messages.put(systemMessage);
        messages.put(userMessage);
        payload.put("messages", messages);
        payload.put("temperature", 0.2);

        try (OutputStream stream = connection.getOutputStream()) {
            stream.write(payload.toString().getBytes(StandardCharsets.UTF_8));
        }

        int status = connection.getResponseCode();
        if (status >= 400) {
            String details = new String(connection.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
            throw new IOException("LLM request failed with HTTP " + status + ": " + details);
        }

        String body = new String(connection.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        JSONObject responseObject = new JSONObject(body);
        JSONArray choices = responseObject.getJSONArray("choices");
        JSONObject firstChoice = choices.getJSONObject(0);
        JSONObject message = firstChoice.getJSONObject("message");
        return message.getString("content").trim();
    }

}
