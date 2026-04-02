package com.hambooking.frontend.service;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

/**
 * Cliente HTTP singleton para la API REST del backend.
 * Soporta GET, POST, PATCH y listas (arrays JSON).
 * Incluye JavaTimeModule para serializar LocalDate/LocalTime correctamente.
 */
public class ApiClient {

    private static final String BASE_URL = "http://localhost:8080/api";
    private static ApiClient instance;

    private final HttpClient   httpClient;
    private final ObjectMapper objectMapper;

    private ApiClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public static ApiClient getInstance() {
        if (instance == null) {
            instance = new ApiClient();
        }
        return instance;
    }

    // ── POST ─────────────────────────────────────────────────────

    public <T> T post(String endpoint, Object body, Class<T> responseType) throws ApiException {
        try {
            String json = objectMapper.writeValueAsString(body);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + endpoint))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .timeout(Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString()
            );

            return handleResponse(response, responseType);

        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("No se pudo conectar con el servidor.", 0);
        }
    }

    // ── GET objeto unico ─────────────────────────────────────────

    public <T> T get(String endpoint, Class<T> responseType) throws ApiException {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + endpoint))
                    .header("Accept", "application/json")
                    .GET()
                    .timeout(Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString()
            );

            return handleResponse(response, responseType);

        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("No se pudo conectar con el servidor.", 0);
        }
    }

    // ── GET lista ────────────────────────────────────────────────

    public <T> List<T> getList(String endpoint, Class<T> elementType) throws ApiException {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + endpoint))
                    .header("Accept", "application/json")
                    .GET()
                    .timeout(Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                JavaType listType = objectMapper.getTypeFactory()
                        .constructCollectionType(List.class, elementType);
                return objectMapper.readValue(response.body(), listType);
            }

            throw new ApiException(
                    extraerMensajeError(response.body(), response.statusCode()),
                    response.statusCode()
            );

        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("No se pudo conectar con el servidor.", 0);
        }
    }

    // ── PUT ──────────────────────────────────────────────────────

    public void put(String endpoint, Object body) throws ApiException {
        try {
            put(endpoint, objectMapper.writeValueAsString(body));
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("No se pudo conectar con el servidor.", 0);
        }
    }

    public void put(String endpoint, String jsonBody) throws ApiException {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + endpoint))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .timeout(Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new ApiException(
                        extraerMensajeError(response.body(), response.statusCode()),
                        response.statusCode()
                );
            }

        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("No se pudo conectar con el servidor.", 0);
        }
    }

    // ── PATCH ────────────────────────────────────────────────────

    public void patch(String endpoint) throws ApiException {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + endpoint))
                    .header("Accept", "application/json")
                    .method("PATCH", HttpRequest.BodyPublishers.noBody())
                    .timeout(Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new ApiException(
                        extraerMensajeError(response.body(), response.statusCode()),
                        response.statusCode()
                );
            }

        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("No se pudo conectar con el servidor.", 0);
        }
    }

    // ── Manejo interno ───────────────────────────────────────────

    private <T> T handleResponse(HttpResponse<String> response,
                                 Class<T> responseType) throws ApiException {
        int    statusCode = response.statusCode();
        String body       = response.body();

        if (statusCode >= 200 && statusCode < 300) {
            try {
                return objectMapper.readValue(body, responseType);
            } catch (Exception e) {
                throw new ApiException("Error al procesar la respuesta del servidor.", statusCode);
            }
        }

        throw new ApiException(extraerMensajeError(body, statusCode), statusCode);
    }

    private String extraerMensajeError(String body, int statusCode) {
        try {
            var node = objectMapper.readTree(body);
            if (node.has("message")) return node.get("message").asText();
        } catch (Exception ignored) {}

        return switch (statusCode) {
            case 400 -> "Datos incorrectos. Revisa el formulario.";
            case 401 -> "Email o contrasena incorrectos.";
            case 403 -> "No tienes permiso para realizar esta accion.";
            case 404 -> "Recurso no encontrado.";
            case 409 -> "Ya existe un registro con esos datos.";
            case 500 -> "Error interno del servidor.";
            case 0   -> "No se pudo conectar con el servidor.";
            default  -> "Error inesperado (codigo " + statusCode + ").";
        };
    }

    // ── Excepcion ────────────────────────────────────────────────

    public static class ApiException extends Exception {
        private final int statusCode;

        public ApiException(String message, int statusCode) {
            super(message);
            this.statusCode = statusCode;
        }

        public int  getStatusCode()        { return statusCode; }
        public boolean isConnectionError() { return statusCode == 0; }
        public boolean isUnauthorized()    { return statusCode == 401; }
        public boolean isConflict()        { return statusCode == 409; }
    }
}