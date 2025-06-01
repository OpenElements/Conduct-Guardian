package com.openelements.conduct.integration.github;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class GitHubClientImpl implements GitHubClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String githubToken;

    public GitHubClientImpl(@Value("${guardian.integration.github.coc.token:}") String githubToken) {
        this.githubToken = githubToken;
    }

    @Override
    public JsonNode getRepositoryFileContent(String owner, String repo, String path, String branch) throws Exception {
        String url = String.format("https://api.github.com/repos/%s/%s/contents/%s?ref=%s", owner, repo, path, branch);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/vnd.github.v3+json");
        if (githubToken != null && !githubToken.isBlank()) {
            headers.setBearerAuth(githubToken);
        }

        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            return objectMapper.readTree(response.getBody());
        }

        throw new RuntimeException("Failed to fetch file from GitHub: " + response.getStatusCode());
    }
}
