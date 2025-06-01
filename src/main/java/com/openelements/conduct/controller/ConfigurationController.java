package com.openelements.conduct.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/config")
public class ConfigurationController {

    private final Environment environment;

    @Autowired
    public ConfigurationController(Environment environment) {
        this.environment = environment;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getConfiguration() {
        Map<String, Object> config = new HashMap<>();
        
        // Only expose non-sensitive configuration
        config.put("application.name", environment.getProperty("spring.application.name", "Conduct Guardian"));
        config.put("discord.enabled", environment.getProperty("guardian.integration.discord.enabled", "false"));
        config.put("slack.enabled", environment.getProperty("guardian.integration.slack.enabled", "false"));
        config.put("openai.enabled", environment.getProperty("guardian.integration.openai.enabled", "false"));
        config.put("openai.model", environment.getProperty("guardian.integration.openai.model", "gpt-3.5-turbo"));
        config.put("github.coc.enabled", environment.getProperty("guardian.integration.github.coc.enabled", "true"));
        config.put("log.enabled", environment.getProperty("guardian.integration.log.enabled", "true"));
        
        return ResponseEntity.ok(config);
    }

    @GetMapping("/integrations")
    public ResponseEntity<IntegrationStatus> getIntegrationStatus() {
        IntegrationStatus status = new IntegrationStatus(
            Boolean.parseBoolean(environment.getProperty("guardian.integration.discord.enabled", "false")),
            Boolean.parseBoolean(environment.getProperty("guardian.integration.slack.enabled", "false")),
            Boolean.parseBoolean(environment.getProperty("guardian.integration.openai.enabled", "false")),
            Boolean.parseBoolean(environment.getProperty("guardian.integration.github.coc.enabled", "true")),
            Boolean.parseBoolean(environment.getProperty("guardian.integration.log.enabled", "true"))
        );
        
        return ResponseEntity.ok(status);
    }

    @PostMapping("/validate")
    public ResponseEntity<ValidationResult> validateConfiguration(@RequestBody ConfigValidationRequest request) {
        // Validate configuration without exposing sensitive data
        boolean isValid = true;
        String message = "Configuration is valid";
        
        // Add validation logic here
        if (request.checkOpenAI() && !hasOpenAIConfig()) {
            isValid = false;
            message = "OpenAI configuration is incomplete";
        }
        
        if (request.checkDiscord() && !hasDiscordConfig()) {
            isValid = false;
            message = "Discord configuration is incomplete";
        }
        
        return ResponseEntity.ok(new ValidationResult(isValid, message));
    }

    private boolean hasOpenAIConfig() {
        return environment.getProperty("guardian.integration.openai.apiKey") != null &&
               !environment.getProperty("guardian.integration.openai.apiKey", "").isEmpty();
    }

    private boolean hasDiscordConfig() {
        return environment.getProperty("guardian.integration.discord.token") != null &&
               !environment.getProperty("guardian.integration.discord.token", "").isEmpty();
    }

    public record IntegrationStatus(
        boolean discordEnabled,
        boolean slackEnabled,
        boolean openaiEnabled,
        boolean githubCocEnabled,
        boolean logEnabled
    ) {}

    public record ConfigValidationRequest(
        boolean checkOpenAI,
        boolean checkDiscord,
        boolean checkSlack,
        boolean checkGitHub
    ) {}

    public record ValidationResult(
        boolean isValid,
        String message
    ) {}
}
