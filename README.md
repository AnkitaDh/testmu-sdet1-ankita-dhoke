# TestMu SDET-1 Selenium Java Assessment

This repository now covers the three assessment tasks requested by the prompt:
- Task 1 acknowledged: the project was scaffolded as a Selenium Java Maven project and the first-commit intent is captured in the delivery notes.
- Task 2 acknowledged: raw prompts and generated Gherkin test cases are included in the repository.
- Task 3 acknowledged: a working LLM integration is included and writes a structured explanation report for failed tests.

## Project structure
- `pom.xml` — Maven project configuration
- `src/test/java/com/testmu` — Selenium, API, and LLM integration test source code
- `src/test/resources/pages` — local HTML pages used for UI tests
- `prompts.md` — raw prompts used for Task 2
- `generated-tests.md` — generated test cases in Gherkin style
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
- The implementation calls an OpenAI-compatible chat completion API when `OPENAI_API_KEY` is available.
- The code now fails fast when no API key is configured, which makes the requirement for a real LLM call explicit and prevents a fake fallback path from being mistaken for a genuine response.
- The sample report is stored in `llm-sample-output.md` and will be generated in `target/llm-reports/failure-explanation.json` during real execution with a configured API key.

## Running tests
1. Install Java 17 or later.
2. Run `mvn test` from the repository root.
3. The UI tests use HtmlUnit through Selenium and the LLM integration writes a report under `target/llm-reports`.

## Notes
This solution is intentionally written in Selenium Java only, as requested.
