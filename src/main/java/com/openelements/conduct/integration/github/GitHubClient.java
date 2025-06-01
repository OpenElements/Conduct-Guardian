package com.openelements.conduct.integration.github;

import com.fasterxml.jackson.databind.JsonNode;

public interface GitHubClient {
    JsonNode getRepositoryFileContent(String owner, String repo, String path, String branch) throws Exception;
}
