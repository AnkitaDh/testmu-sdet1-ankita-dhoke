# LLM Integration Sample Output

The failure explainer writes a structured report to target/llm-reports/failure-explanation.json when a real OpenAI-compatible API call succeeds. The current implementation fails fast if OPENAI_API_KEY is not configured so reviewers can see that the integration is real rather than silently using a fallback.

Example report shape:

```json
{
  "testName": "LoginTests.validLoginRedirectsToDashboard",
  "source": "openai",
  "response": "The failure appears to be caused by the login form remaining on the page after submit. The suggested fix is to verify that the submit handler is redirecting to the dashboard and that the expected auth token is being stored before navigation.",
  "pageState": "The login form stayed on the page after submit.",
  "apiResponse": "HTTP 500 from the mock dashboard endpoint"
}
```
