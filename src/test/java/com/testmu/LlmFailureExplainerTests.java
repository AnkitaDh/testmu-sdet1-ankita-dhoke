package com.testmu;

import com.testmu.llm.LlmFailureExplainer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LlmFailureExplainerTests {
    @Test
    void failsFastWhenNoApiKeyIsConfigured() {
        LlmFailureExplainer explainer = new LlmFailureExplainer(null, "gpt-4o-mini", "https://api.openai.com/v1", null);
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> explainer.explainFailure(
                "LoginTests.validLoginRedirectsToDashboard",
                "The login form stayed on the page after submit.",
                "HTTP 500 from the mock dashboard endpoint"
        ));
        assertTrue(exception.getMessage().contains("OPENAI_API_KEY"));
    }
}
