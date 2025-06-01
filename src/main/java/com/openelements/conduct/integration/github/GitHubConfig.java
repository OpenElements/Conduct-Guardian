package com.openelements.conduct.integration.github;

import com.openelements.conduct.data.CodeOfConductProvider;
import com.openelements.conduct.endpoint.GitHubClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@ConditionalOnProperty(
        name = "guardian.integration.github.coc.enabled",
        havingValue = "true",
        matchIfMissing = false
)
public class GitHubConfig {

    @Value("${guardian.integration.github.coc.owner:OpenElements}")
    private String owner;

    @Value("${guardian.integration.github.coc.repo:Conduct-Guardian}")
    private String repo;

    @Bean
    @Primary
    public CodeOfConductProvider githubCodeOfConductProvider(GitHubClient gitHubClient) {
        return new GitHubCodeOfConductProvider(gitHubClient, owner, repo);
    }
}
