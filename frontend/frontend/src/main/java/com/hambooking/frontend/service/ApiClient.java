package com.hambooking.frontend.service;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Cliente HTTP singleton para comunicarse con la API REST del backend.
 *
 * Uso:
 *   ApiClient client = ApiClient.getInstance();
 *   LoginResponse response = client.post("/auth/login", request, LoginResponse.class);
 *
 * La URL base apunta a localhost:8080 donde corre el backend Spring Boot.
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
    }

    public static ApiClient getInstance() {
        if (instance == null) {
            instance = new ApiClient();
        }
        return instance;
    }

    // ── POST ─────────────────────────────────────────────────────

    /**
     * Envia una peticion POST con cuerpo JSON.
     *
     * @param endpoint  ruta relativa, ej: "/auth/login"
     * @param body      objeto Java que se serializa a JSON
     * @param responseType clase esperada en la respuesta
     * @return objeto deserializado de la respuesta
     * @throws ApiException si el servidor devuelve un error HTTP
     */
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
            throw new ApiException("No se pudo conectar con el servidor. Verifica que el backend este corriendo.", 0);
        }
    }

    // ── GET ──────────────────────────────────────────────────────

    /**
     * Envia una peticion GET.
     *
     * @param endpoint     ruta relativa, ej: "/services"
     * @param responseType clase esperada en la respuesta
     * @return objeto deserializado de la respuesta
     * @throws ApiException si el servidor devuelve un error HTTP
     */
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
            throw new ApiException("No se pudo conectar con el servidor. Verifica que el backend este corriendo.", 0);
        }
    }

    // ── Manejo de respuestas ─────────────────────────────────────

    private <T> T handleResponse(HttpResponse<String> response,
                                 Class<T> responseType) throws ApiException {
        int statusCode = response.statusCode();
        String body    = response.body();

        if (statusCode >= 200 && statusCode < 300) {
            try {
                return objectMapper.readValue(body, responseType);
            } catch (Exception e) {
                throw new ApiException("Error al procesar la respuesta del servidor.", statusCode);
            }
        }

        // Intentar extraer el mensaje de error del backend
        String errorMessage = extraerMensajeError(body, statusCode);
        throw new ApiException(errorMessage, statusCode);
    }

    private String extraerMensajeError(String body, int statusCode) {
        try {
            // El backend devuelve { "status": 401, "message": "...", "timestamp": "..." }
            var node = objectMapper.readTree(body);
            if (node.has("message")) {
                return node.get("message").asText();
            }
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

    // ── Excepcion personalizada ───────────────────────────────────

    /**
     * Excepcion que lanza ApiClient cuando el servidor devuelve un error.
     * Los controladores la capturan para mostrar el mensaje al usuario.
     */
    public static class ApiException extends Exception {
        private final int statusCode;

        public ApiException(String message, int statusCode) {
            super(message);
            this.statusCode = statusCode;
        }

        public int getStatusCode() { return statusCode; }

        public boolean isConnectionError() { return statusCode == 0; }
        public boolean isUnauthorized()    { return statusCode == 401; }
        public boolean isConflict()        { return statusCode == 409; }
    }
}