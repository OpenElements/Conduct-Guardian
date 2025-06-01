package com.openelements.conduct.integration.github;

import com.fasterxml.jackson.databind.JsonNode;
import com.openelements.conduct.data.CodeOfConductProvider;
import com.openelements.conduct.data.TextfileType;
import com.openelements.conduct.endpoint.GitHubClient;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GitHubCodeOfConductProvider implements CodeOfConductProvider {

    private static final Logger log = LoggerFactory.getLogger(GitHubCodeOfConductProvider.class);

    private static final String[] COMMON_COC_FILENAMES = {
            "CODE_OF_CONDUCT.md", "CODE_OF_CONDUCT.txt", "CODE_OF_CONDUCT",
            "CODE-OF-CONDUCT.md", "code-of-conduct.md", "code_of_conduct.md",
            "CONDUCT.md", "CONDUCT.txt"
    };

    private final String codeOfConduct;

    public GitHubCodeOfConductProvider(@NonNull GitHubClient gitHubClient,
                                       @NonNull String owner,
                                       @NonNull String repo) {
        log.info("Initialized GitHub Code of Conduct provider for {}/{}", owner, repo);
        this.codeOfConduct = findCodeOfConduct(gitHubClient, owner, repo);
    }

    private String findCodeOfConduct(@NonNull GitHubClient gitHubClient,
                                     @NonNull String owner,
                                     @NonNull String repo) {
        return findCodeOfConduct(gitHubClient, owner, repo, "main")
                .or(() -> findCodeOfConduct(gitHubClient, owner, repo, "master"))
                .or(() -> findCodeOfConduct(gitHubClient, owner, repo, "staging"))
                .or(() -> findCodeOfConduct(gitHubClient, owner, ".github", "main"))
                .or(() -> findCodeOfConduct(gitHubClient, owner, ".github", "master"))
                .orElseThrow(() -> new RuntimeException("No Code of Conduct file found for " + owner + "/" + repo));
    }

    private Optional<String> findCodeOfConduct(GitHubClient client, String owner, String repo, String branch) {
        try {
            for (String filename : COMMON_COC_FILENAMES) {
                try {
                    JsonNode file = client.getRepositoryFileContent(owner, repo, filename, branch);
                    if (file != null && file.has("content")) {
                        String encoded = file.get("content").asText().replace("\n", "");
                        String decoded = new String(Base64.getDecoder().decode(encoded));
                        log.info("Found Code of Conduct file '{}' in {}/{}#{}", filename, owner, repo, branch);
                        return Optional.of(decoded);
                    }
                } catch (Exception e) {
                    log.debug("Could not fetch file {} in {}/{}#{}", filename, owner, repo, branch);
                }
            }
        } catch (Exception e) {
            log.error("Failed to search for Code of Conduct file in {}/{}#{}", owner, repo, branch, e);
        }
        return Optional.empty();
    }

    @Override
    public boolean supports(@NonNull TextfileType type) {
        return true;
    }

    @Override
    public @NonNull String getCodeOfConduct(@NonNull TextfileType type) {
        return codeOfConduct;
    }
}
