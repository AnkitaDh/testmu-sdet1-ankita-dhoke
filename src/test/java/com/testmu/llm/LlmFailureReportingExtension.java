package com.testmu.llm;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;

public class LlmFailureReportingExtension implements TestWatcher {
    private final LlmFailureExplainer explainer;

    public LlmFailureReportingExtension() {
        this(new LlmFailureExplainer());
    }

    public LlmFailureReportingExtension(LlmFailureExplainer explainer) {
        this.explainer = explainer;
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        String testName = context.getRequiredTestClass().getSimpleName() + "." + context.getRequiredTestMethod().getName();
        String pageState = "A test failure was observed by the JUnit extension.";
        String apiResponse = cause.getMessage() == null ? cause.getClass().getSimpleName() : cause.getMessage();
        reportFailure(testName, pageState, apiResponse);
    }

    public void reportFailure(String testName, String pageState, String apiResponse) {
        try {
            explainer.explainFailure(testName, pageState, apiResponse);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to generate LLM failure explanation: " + exception.getMessage(), exception);
        }
    }
}
