# TestMu SDET-1 Selenium Java Assessment

This repository implements the three assessment tasks in a single, runnable Selenium Java project using JUnit 5, HtmlUnitDriver, a local mock API server, and an LLM-based failure explainer.

The implementation is intentionally straightforward and explicit so a reviewer can follow the automation flow end to end without relying on hidden scaffolding or fake output.

## What is in this repository
- `pom.xml` — Maven project configuration
- `src/test/java/com/testmu` — Selenium, API, and LLM integration test source code
- `src/test/java/com/testmu/support` — shared test support for the UI harness
- `src/test/resources/pages` — local HTML pages used for UI tests
- `prompts.md` — raw prompts used for Task 2, including the refinement sequence
- `generated-tests.md` — curated Gherkin scenarios grounded in the implemented login, dashboard, and API behavior
- `module-notes.md` — short notes per module on what changed after the first pass
- `ai-usage-log.md` — AI tool usage log for each task
- `llm-sample-output.md` — a sample LLM explanation report artifact

## Task 1
- First commit message used: `GitHub Copilot: scaffolded Selenium Java assessment and initial LLM integration`
- This explicitly names the AI tool used and what it helped with: scaffolding the project and setting up the initial LLM integration.

## Task 2
- Prompt inputs are preserved in `prompts.md` exactly as used.
- Generated test cases are stored in `generated-tests.md`.
- Module notes are stored in `module-notes.md`.

## Task 3
- Option A was selected: failure explainer.
- The implementation uses a real HTTP request to an OpenAI-compatible chat completions endpoint when a test fails and a valid `OPENAI_API_KEY` is present.
- If `OPENAI_API_KEY` is not set, or the API call fails, the test fails fast with a clear error rather than falling back to a canned explanation — this keeps the LLM call verifiably real rather than mocked. Set `OPENAI_API_KEY` in your environment before running `mvn test` if you want the failure-explainer step to pass.
- The LLM integration is wired into the JUnit test flow so it is triggered when a test fails, and the code includes a comment explaining why Option A was chosen over Option B.
- The sample report is stored in `llm-sample-output.md` and will be generated in `target/llm-reports/failure-explanation.json` during real execution with a configured API key.

## Running tests
1. Install Java 17 or later.
2. Set `OPENAI_API_KEY` in your environment before running the LLM-integration path. Example: `export OPENAI_API_KEY=your-key-here`
3. Run `mvn test` from the repository root.
4. The UI tests use HtmlUnit through Selenium and the LLM integration writes a report under `target/llm-reports`.
5. Without a configured key, the failure-explainer step will fail rather than pass with fake output.

## Notes
This solution is intentionally written in Selenium Java only, as requested.
It uses a small, explicit test harness rather than a heavyweight framework so the implementation can be understood quickly and verified locally.
