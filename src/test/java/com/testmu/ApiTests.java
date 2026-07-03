package com.testmu;

import com.testmu.server.SimpleApiServer;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class ApiTests {
    private static SimpleApiServer server;
    private static final HttpClient client = HttpClient.newHttpClient();

    @BeforeAll
    static void startServer() throws Exception {
        server = new SimpleApiServer(5050);
        server.start();
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    void resetServerState() {
        server.resetState();
    }

    @Test
    void authTokenValidationSucceedsForProtectedEndpoint() throws Exception {
        String token = loginAndGetToken();
        var request = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:5050/api/items"))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertEquals(200, response.statusCode());
    }

    @Test
    void crudOperationsWorkForItemsApi() throws Exception {
        String token = loginAndGetToken();
        var createRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:5050/api/items"))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(new JSONObject().put("name", "New Widget").put("description", "Created by test").put("value", 55).toString()))
                .build();
        var createResponse = client.send(createRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertEquals(201, createResponse.statusCode());
        var created = new JSONObject(createResponse.body());
        assertEquals("New Widget", created.getString("name"));

        String itemId = created.getString("id");
        var getRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:5050/api/items/" + itemId))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();
        var getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertEquals(200, getResponse.statusCode());

        var updateRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:5050/api/items/" + itemId))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(new JSONObject().put("value", 60).toString()))
                .build();
        var updateResponse = client.send(updateRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertEquals(200, updateResponse.statusCode());
        assertEquals(60, new JSONObject(updateResponse.body()).getInt("value"));

        var deleteRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:5050/api/items/" + itemId))
                .header("Authorization", "Bearer " + token)
                .DELETE()
                .build();
        var deleteResponse = client.send(deleteRequest, HttpResponse.BodyHandlers.discarding());
        assertEquals(204, deleteResponse.statusCode());
    }

    @Test
    void errorHandlingReturns400And500StatusCodes() throws Exception {
        String token = loginAndGetToken();
        var invalidCreate = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:5050/api/items"))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(new JSONObject().put("name", "").toString()))
                .build();
        assertEquals(400, client.send(invalidCreate, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)).statusCode());

        var boomResponse = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:5050/api/boom"))
                .GET()
                .build();
        assertEquals(500, client.send(boomResponse, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)).statusCode());
    }

    @Test
    void rateLimitingBlocksRepeatedRequests() throws Exception {
        String token = loginAndGetToken();
        for (int i = 0; i < 5; i++) {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create("http://127.0.0.1:5050/api/items"))
                    .header("Authorization", "Bearer " + token)
                    .GET()
                    .build();
            assertEquals(200, client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)).statusCode());
        }
        var blockedRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:5050/api/items"))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();
        assertEquals(429, client.send(blockedRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)).statusCode());
    }

    @Test
    void schemaValidationReturnsExpectedItemStructure() throws Exception {
        var request = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:5050/api/schema"))
                .GET()
                .build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertEquals(200, response.statusCode());
        var schema = new JSONObject(response.body());
        assertTrue(schema.has("properties"));
        assertTrue(schema.getJSONObject("properties").getJSONObject("items").getJSONObject("items").getJSONObject("properties").has("id"));
    }

    private String loginAndGetToken() throws Exception {
        var request = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:5050/api/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(new JSONObject().put("email", "tester@testmu.ai").put("password", "TestMu123!").toString()))
                .build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertEquals(200, response.statusCode());
        return new JSONObject(response.body()).getString("token");
    }
}
