package edu.una.lab612.consumirapi.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import edu.una.lab612.consumirapi.domain.Repo;
import edu.una.lab612.consumirapi.domain.User;

import java.net.http.*;
import java.net.URI;
import java.time.Duration;
import java.io.IOException;
import java.util.List;

public class GitHubApi {
    private final HttpClient client;
    private final ObjectMapper mapper;

    public GitHubApi() {
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.mapper = new ObjectMapper();
    }

    private HttpRequest.Builder baseRequest(String url) {
        HttpRequest.Builder b = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .header("Accept", "application/vnd.github+json")
                .header("User-Agent", "java-github-client");
        String token = System.getenv("GITHUB_TOKEN");
        if (token != null && !token.isBlank()) {
            b.header("Authorization", "Bearer " + token);
        }
        return b;
    }

    public User getUser(String username) throws IOException, InterruptedException, ApiException {
        String url = "https://api.github.com/users/" + username;
        HttpRequest req = baseRequest(url).GET().build();
        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
        int status = res.statusCode();
        if (status == 404) throw new ApiException("Usuario no encontrado (404).");
        if (status == 403) throw new ApiException("Límite de peticiones alcanzado o acceso denegado (403). "
                + parseRateLimit(res));
        if (status >= 400) throw new ApiException("Error al obtener usuario: HTTP " + status);
        return mapper.readValue(res.body(), User.class);
    }

    public List<Repo> getRepos(String username) throws IOException, InterruptedException, ApiException {
        String url = "https://api.github.com/users/" + username + "/repos?per_page=100&sort=updated";
        HttpRequest req = baseRequest(url).GET().build();
        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
        int status = res.statusCode();
        if (status == 404) throw new ApiException("Repositorios no encontrados (404).");
        if (status == 403) throw new ApiException("Límite de peticiones alcanzado o acceso denegado (403). "
                + parseRateLimit(res));
        if (status >= 400) throw new ApiException("Error al obtener repos: HTTP " + status);
        return mapper.readValue(res.body(), new TypeReference<List<Repo>>() {});
    }

    private String parseRateLimit(HttpResponse<?> res) {
        String limit = res.headers().firstValue("X-RateLimit-Limit").orElse("?");
        String remaining = res.headers().firstValue("X-RateLimit-Remaining").orElse("?");
        String reset = res.headers().firstValue("X-RateLimit-Reset").orElse("?");
        return String.format("rate limit: %s, remaining: %s, reset: %s (epoch sec)", limit, remaining, reset);
    }

    public static class ApiException extends Exception {
        public ApiException(String message) { super(message); }
    }
}
